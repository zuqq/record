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
 * A git repository.
 *
 * An instance of this class consists of the working directory's location
 * (i.e., {@link #directory}) and a number of methods that operate on the
 * working directory. Everything else ({@link Blob}s, {@link Commit}s, etc.)
 * is stored in the .git subdirectory.
 */
public final class Repository {
    private final Path directory;
    private final Path gitDirectory;

    public Repository(Path directory) {
        this.directory = directory;
        this.gitDirectory = directory.resolve(".git");
    }

    /**
     * Reads a reference.
     *
     * The parameter {@code name} should be a fully qualified name such as "HEAD"
     * or "refs/heads/master".
     */
    private ReferenceContent readReference(String name) throws IOException {
        String content = Files.readString(gitDirectory.resolve(name));
        // Remove trailing '\n'.
        return new ReferenceContent(content.substring(0, content.length() - 1));
    }

    /**
     * Writes a reference.
     *
     * The parameter {@code name} should be a fully qualified name such as "HEAD"
     * or "refs/heads/master".
     */
    private void writeReference(String name, ReferenceContent content) throws IOException {
        // Add trailing '\n'.
        Files.writeString(gitDirectory.resolve(name), content.toString() + "\n");
    }

    /**
     * Recursively resolves the given reference.
     *
     * Reads the reference {@code name} and follows symbolic references until
     * it finds one that is null (i.e., the corresponding file doesn't exist)
     * or points to a commit. Returns the name of that final reference.
     *
     * Sets a maximal number of redirects to break cycles.
     */
    private String resolveReference(String name) throws IOException {
        for (int i = 0; i < 5; ++i) {
            if (!Files.exists(gitDirectory.resolve(name))) {
                return name;
            }
            ReferenceContent content = readReference(name);
            if (!content.isSymbolic()) {
                return name;
            }
            name = content.getTarget();
        }
        throw new RuntimeException("Too many redirects.");
    }

    /**
     * Initializes the .git subdirectory.
     *
     * @throws IOException If one of the required directories and files could not be created.
     */
    public void init() throws IOException {
        if (!Files.exists(gitDirectory)) {
            Files.createDirectories(gitDirectory.resolve("objects"));
            Files.createDirectories(gitDirectory.resolve("refs/heads"));
            Files.createDirectories(gitDirectory.resolve("refs/tags"));
            writeReference("HEAD", new ReferenceContent(true, "refs/heads/master"));
        }
    }

    /**
     * Reads a {@link LooseObject} from the object store.
     *
     * Note that this returns the inflated (i.e., decompressed) content.
     *
     * The parameter {@code encodedHash} is the object's Base16-encoded hash.
     */
    private byte[] readObject(String encodedHash) throws IOException {
        Path bucket = gitDirectory.resolve("objects").resolve(encodedHash.substring(0, 2));
        try (InputStream file = Files.newInputStream(bucket.resolve(encodedHash.substring(2)));
             InflaterInputStream stream = new InflaterInputStream(file)) {
            return stream.readAllBytes();
        }
    }

    /**
     * Reads a {@link LooseObject} from the object store.
     *
     * Note that this returns the inflated (i.e., decompressed) content.
     *
     * The parameter {@code hash} is the object's hash.
     */
    private byte[] readObject(byte[] hash) throws IOException {
        return readObject(Base16.encode(hash));
    }

    /**
     * Writes a {@link LooseObject} to the object store.
     *
     * Note that this deflates (i.e., compresses) the content.
     */
    private void writeObject(LooseObject object) throws IOException {
        String encodedHash = Base16.encode(object.getHash());
        Path bucket = gitDirectory.resolve("objects").resolve(encodedHash.substring(0, 2));
        if (!Files.exists(bucket)) {
            Files.createDirectory(bucket);
        }
        Path path = bucket.resolve(encodedHash.substring(2));
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
     * Note that this writes the resulting git objects to the file system.
     *
     * Returns the Base16-encoded hash of the {@link Tree} corresponding to
     * the current state of the working directory.
     */
    private String freezeTree() throws IOException {
        TreeFreezer visitor = new TreeFreezer(directory);
        Files.walkFileTree(directory, visitor);
        return visitor.getResult();
    }

    /**
     * Commits the entire working directory.
     *
     * Creates a new commit object reflecting the current state of the working
     * directory, writes the commit object to the object store, and then
     * advances the HEAD.
     *
     * @param committer Who is creating the commit.
     * @param message The commit message.
     * @throws IOException If one of the steps failed.
     */
    public void commit(User committer, String message) throws IOException {
        String head = resolveReference("HEAD");
        List<String> parents = new ArrayList<>();
        if (Files.exists(gitDirectory.resolve(head))) {
            parents.add(readReference(head).getTarget());
        }
        Timestamp timestamp = Timestamp.now();
        Commit commit = new Commit(freezeTree(), parents, committer, timestamp, committer, timestamp, message);
        writeObject(commit);
        writeReference(head, new ReferenceContent(commit));
    }

    private class TreeThawer implements TreeVisitor<IOException> {
        private Path currentDirectory;

        public TreeThawer() {
            this.currentDirectory = directory;
        }

        @Override
        public void visit(Directory node) throws IOException {
            currentDirectory = currentDirectory.resolve(node.getName());
            Tree.parse(readObject(node.getObjectHash())).accept(this);
            currentDirectory = currentDirectory.getParent();
        }

        @Override
        public void visit(File node) throws IOException {
            Path path = currentDirectory.resolve(node.getName());
            Files.write(path, Blob.parse(readObject(node.getObjectHash())).getBody());
            Files.setPosixFilePermissions(path,
                    PosixFilePermissions.fromString(node.isExecutable() ? "rwxr-xr-x" : "rw-r--r--"));
        }

        @Override
        public void visit(SymbolicLink node) throws IOException {
            Files.createSymbolicLink(currentDirectory.resolve(node.getName()),
                    Path.of(new String(Blob.parse(readObject(node.getObjectHash())).getBody(),
                            StandardCharsets.UTF_8)));
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
     * Restores the working directory to the state of {@code encodedCommitHash}.
     *
     * Note this is a destructive operation: it discards the current state of
     * the working directory.
     *
     * @param encodedCommitHash The Base16-encoded hash of the commit to check out.
     * @throws IOException If the checkout failed.
     */
    public void checkout(String encodedCommitHash) throws IOException {
        byte[] treeHash = Commit.extractTreeHash(readObject(encodedCommitHash));
        Files.walkFileTree(directory, new TreeClearer());
        thawTree(Tree.parse(readObject(treeHash)));
        writeReference("HEAD", new ReferenceContent(encodedCommitHash));
    }
}
