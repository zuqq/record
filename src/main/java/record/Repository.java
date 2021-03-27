package record;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * A Git repository.
 *
 * <p>An instance of this class consists of the working directory's location
 * (i.e., {@link #directory}) and a number of methods that operate on the
 * working directory. Everything else ({@link Blob}s, {@link Commit}s, etc.)
 * is stored in the {@code .git} subdirectory.
 */
public final class Repository {
    private static final String OBJECT_PREFIX = "objects/";
    private static final String BRANCH_PREFIX = "refs/heads/";
    private static final String HEAD = "HEAD";

    private final Path directory;
    private final Path gitDirectory;

    public Repository(Path directory) {
        this.directory = directory;
        this.gitDirectory = directory.resolve(".git");
    }

    /**
     * Reads a reference.
     *
     * @param name A fully qualified reference name such as {@code "HEAD"} or
     *             {@code "refs/heads/master"}.
     */
    private Reference readReference(String name) throws IOException {
        return Reference.of(name, Files.readString(gitDirectory.resolve(name), StandardCharsets.UTF_8));
    }

    /**
     * Writes a reference.
     */
    private void writeReference(Reference reference) throws IOException {
        Files.writeString(gitDirectory.resolve(reference.getName()), reference.toString(), StandardCharsets.UTF_8);
    }

    /**
     * Initializes the {@code .git} subdirectory.
     *
     * @throws IOException If one of the required directories and files could
     *                     not be created.
     */
    public void init() throws IOException {
        if (!Files.exists(gitDirectory)) {
            Files.createDirectories(gitDirectory.resolve(OBJECT_PREFIX));
            Files.createDirectories(gitDirectory.resolve(BRANCH_PREFIX));
            writeReference(new Reference(HEAD, true, BRANCH_PREFIX + "master"));
        }
    }

    private Path getObjectPath(String encodedHash) {
        return gitDirectory
                .resolve(OBJECT_PREFIX)
                .resolve(encodedHash.substring(0, 2))
                .resolve(encodedHash.substring(2));
    }

    private Path getObjectPath(byte[] hash) {
        return getObjectPath(Base16.encode(hash));
    }

    /**
     * Reads a {@link LooseObject} from the object store.
     *
     * <p>Note that this returns the inflated (i.e., decompressed) content.
     *
     * @param encodedHash The object's Base16-encoded hash.
     */
    private byte[] readObject(String encodedHash) throws IOException {
        try (InputStream file = Files.newInputStream(getObjectPath(encodedHash));
             InflaterInputStream stream = new InflaterInputStream(file)) {
            return stream.readAllBytes();
        }
    }

    /**
     * Reads a {@link LooseObject} from the object store.
     *
     * <p>Note that this returns the inflated (i.e., decompressed) content.
     *
     * @param hash The object's hash.
     */
    private byte[] readObject(byte[] hash) throws IOException {
        return readObject(Base16.encode(hash));
    }

    /**
     * Writes a {@link LooseObject} to the object store.
     *
     * <p>Note that this deflates (i.e., compresses) the content.
     */
    private void writeObject(LooseObject object) throws IOException {
        Path path = getObjectPath(object.getHash());
        Path bucket = path.getParent();
        if (!Files.exists(bucket)) {
            Files.createDirectory(bucket);
        }
        if (!Files.exists(path)) {
            try (OutputStream file = Files.newOutputStream(path);
                 DeflaterOutputStream stream = new DeflaterOutputStream(file)) {
                stream.write(object.getBytes());
            }
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("r--r--r--"));
        }
    }

    private class TreeFreezer extends SimpleFileVisitor<Path> {
        private final Path target;
        private final Map<Path, TreeNode> store = new HashMap<>();
        private String result = null;

        public TreeFreezer(Path target) {
            this.target = target;
        }

        public String getResult() {
            return result;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return Files.isHidden(dir) ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc != null) {
                throw exc;
            }
            List<TreeNode> children = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path child : stream) {
                    if (!Files.isHidden(child)) {
                        TreeNode node = store.get(child);
                        if (node == null) {
                            throw new RuntimeException("The algorithm is broken!");
                        }
                        children.add(node);
                        store.remove(child);
                    }
                }
            }
            Tree tree = new Tree(children);
            writeObject(tree);
            byte[] treeHash = tree.getHash();
            store.put(dir, new Directory(dir.getFileName().toString(), treeHash));
            if (dir.equals(target)) {
                result = Base16.encode(treeHash);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (!Files.isHidden(file)) {
                Blob blob;
                TreeNode node;
                if (Files.isSymbolicLink(file)) {
                    blob = new Blob(Files.readSymbolicLink(file).toString().getBytes(StandardCharsets.UTF_8));
                    node = new SymbolicLink(file.getFileName().toString(), blob.getHash());
                } else {
                    blob = new Blob(Files.readAllBytes(file));
                    node = new File(file.getFileName().toString(), Files.isExecutable(file), blob.getHash());
                }
                writeObject(blob);
                store.put(file, node);
            }
            return FileVisitResult.CONTINUE;
        }
    }

    /**
     * Takes a snapshot of the working directory.
     *
     * <p>Note that this writes the resulting Git objects to the file system.
     *
     * @return The Base16-encoded hash of the {@link Tree} corresponding to the
     *         current state of the working directory.
     */
    private String freezeTree() throws IOException {
        TreeFreezer visitor = new TreeFreezer(directory);
        Files.walkFileTree(directory, visitor);
        return visitor.getResult();
    }

    /**
     * Commits the entire working directory.
     *
     * <p>Creates a new commit object reflecting the current state of the
     * working directory, writes the commit object to the object store, and
     * then advances {@code HEAD}.
     *
     * @param committer Who is creating the commit.
     * @param timestamp When the commit is being created.
     * @param message   The commit message.
     * @throws IOException If one of the steps failed.
     */
    public void commit(User committer, Timestamp timestamp, String message) throws IOException {
        Reference head = readReference(HEAD);
        String resolvedName = head.isSymbolic() ? head.getTarget() : HEAD;
        List<String> parents = new ArrayList<>();
        if (Files.exists(gitDirectory.resolve(resolvedName))) {
            parents.add(readReference(resolvedName).getTarget());
        }
        Commit commit = new Commit(freezeTree(), parents, committer, timestamp, committer, timestamp, message);
        writeObject(commit);
        writeReference(new Reference(resolvedName, commit));
    }

    /**
     * Creates a new branch that points to the current commit.
     *
     * @param name Name of the branch.
     * @throws IOException If the new branch couldn't be created.
     */
    public void branch(String name) throws IOException {
        Reference head = readReference(HEAD);
        String encodedHash = head.isSymbolic() ? readReference(head.getTarget()).getTarget() : head.getTarget();
        writeReference(new Reference(BRANCH_PREFIX + name, false, encodedHash));
    }

    private class TreeThawer implements TreeNodeVisitor<IOException> {
        private Path currentDirectory;

        public TreeThawer() {
            this.currentDirectory = directory;
        }

        @Override
        public void visit(Directory node) throws IOException {
            currentDirectory = currentDirectory.resolve(node.getName());
            Files.createDirectories(currentDirectory);
            Tree.parse(readObject(node.getObjectHash())).accept(this);
            currentDirectory = currentDirectory.getParent();
        }

        @Override
        public void visit(File node) throws IOException {
            Path path = currentDirectory.resolve(node.getName());
            Files.write(path, Blob.parse(readObject(node.getObjectHash())).getBody());
            Files.setPosixFilePermissions(
                    path, PosixFilePermissions.fromString(node.isExecutable() ? "rwxr-xr-x" : "rw-r--r--"));
        }

        @Override
        public void visit(SymbolicLink node) throws IOException {
            byte[] body = Blob.parse(readObject(node.getObjectHash())).getBody();
            Files.createSymbolicLink(
                    currentDirectory.resolve(node.getName()), Path.of(new String(body, StandardCharsets.UTF_8)));
        }
    }

    /**
     * Replaces the working directory by {@code tree}.
     */
    private void thawTree(Tree tree) throws IOException {
        tree.accept(new TreeThawer());
    }

    private static class TreeClearer extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return Files.isHidden(dir) ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                if (!stream.iterator().hasNext()) {
                    Files.delete(dir);
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (!Files.isHidden(file)) {
                Files.delete(file);
            }
            return FileVisitResult.CONTINUE;
        }
    }

    /**
     * Restores the working directory to the state of {@code name}.
     *
     * <p>Note this is a destructive operation: it discards the current state of
     * the working directory.
     *
     * @param name The branch name or Base16-encoded commit hash to check out.
     * @throws IOException If the checkout failed.
     */
    public void checkout(String name) throws IOException {
        String branch = BRANCH_PREFIX + name;
        Reference newHead;
        String encodedCommitHash;
        // Determine whether we're given a branch or commit. It's not enough to
        // look at the name, because branches can be named after commits.
        if (Files.exists(gitDirectory.resolve(branch))) {
            newHead = new Reference(HEAD, true, branch);
            encodedCommitHash = readReference(branch).getTarget();
        } else {
            newHead = new Reference(HEAD, false, name);
            encodedCommitHash = name;
        }
        Files.walkFileTree(directory, new TreeClearer());
        byte[] treeHash = Commit.extractTreeHash(readObject(encodedCommitHash));
        thawTree(Tree.parse(readObject(treeHash)));
        writeReference(newHead);
    }
}
