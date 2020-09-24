package record;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public final class Repository {
    private final Path folder;
    private final Path git;

    public Repository(Path folder) {
        this.folder = folder;
        this.git = folder.resolve(".git");
    }

    private ReferenceContent readReference(String name) throws IOException {
        String content = Files.readString(git.resolve(name));
        // Remove trailing '\n'.
        return new ReferenceContent(content.substring(0, content.length() - 1));
    }

    private void writeReference(String name, ReferenceContent content) throws IOException {
        // Add trailing '\n'.
        Files.writeString(git.resolve(name), content.toString() + "\n");
    }

    private String resolveReference(String name) throws IOException {
        // Follow the reference until we find one that is empty (i.e., the
        // corresponding file doesn't exist) or points to a commit (i.e., it is
        // not symbolic). Set a maximal number of hops to avoid looping forever.
        for (int i = 0; i < 5; ++i) {
            if (!Files.exists(git.resolve(name))) {
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

    public void init() throws IOException {
        if (!Files.exists(git)) {
            Files.createDirectories(git.resolve("objects"));
            Files.createDirectories(git.resolve("refs/heads"));
            Files.createDirectories(git.resolve("refs/tags"));
            writeReference("HEAD", new ReferenceContent(true, "refs/heads/master"));
        }
    }

    private byte[] readObject(String encodedHash) throws IOException {
        Path bucket = git.resolve("objects").resolve(encodedHash.substring(0, 2));
        try (InputStream file = Files.newInputStream(bucket.resolve(encodedHash.substring(2)));
             InflaterInputStream stream = new InflaterInputStream(file)) {
            return stream.readAllBytes();
        }
    }

    private byte[] readObject(byte[] hash) throws IOException {
        return readObject(Base16.encode(hash));
    }

    private void writeObject(LooseObject object) throws IOException {
        String encodedHash = Base16.encode(object.getHash());
        Path bucket = git.resolve("objects").resolve(encodedHash.substring(0, 2));
        if (!Files.exists(bucket)) {
            Files.createDirectory(bucket);
        }
        try (OutputStream file = Files.newOutputStream(bucket.resolve(encodedHash.substring(2)));
             DeflaterOutputStream stream = new DeflaterOutputStream(file)) {
            stream.write(object.getBytes());
        }
    }

    private class TreeBuilder extends SimpleFileVisitor<Path> {
        private final Path target;
        private final Map<Path, TreeNode> store = new HashMap<>();
        private String result = null;

        public TreeBuilder(Path target) {
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
                    String name = file.getFileName().toString();
                    byte[] blobHash = blob.getHash();
                    node = Files.isExecutable(file) ? new Executable(name, blobHash) : new File(name, blobHash);
                }
                writeObject(blob);
                store.put(file, node);
            }
            return FileVisitResult.CONTINUE;
        }
    }

    private String readTree() throws IOException {
        TreeBuilder visitor = new TreeBuilder(folder);
        Files.walkFileTree(folder, visitor);
        return visitor.getResult();
    }

    public void commit(User committer, String message) throws IOException {
        String head = resolveReference("HEAD");
        List<String> parents = new ArrayList<>();
        if (Files.exists(git.resolve(head))) {
            parents.add(readReference(head).getTarget());
        }
        Timestamp timestamp = new Timestamp(ZonedDateTime.now());
        Commit commit = new Commit(readTree(), parents, committer, timestamp, committer, timestamp, message);
        writeObject(commit);
        writeReference(head, new ReferenceContent(commit));
    }

    private class TreeWriter implements TreeVisitor<IOException> {
        private Path folder;

        public TreeWriter(Path folder) {
            this.folder = folder;
        }

        @Override
        public void visit(Directory node) throws IOException {
            folder = folder.resolve(node.getName());
            Tree.parse(readObject(node.getTargetHash())).accept(this);
            folder = folder.getParent();
        }

        @Override
        public void visit(Executable node) throws IOException {
            Path path = folder.resolve(node.getName());
            Files.write(path, Blob.parse(readObject(node.getTargetHash())).getBody());
            Files.setPosixFilePermissions(path,
                    Set.of(PosixFilePermission.OWNER_EXECUTE,
                           PosixFilePermission.GROUP_EXECUTE,
                           PosixFilePermission.OTHERS_EXECUTE));
        }

        @Override
        public void visit(File node) throws IOException {
            Files.write(folder.resolve(node.getName()),
                        Blob.parse(readObject(node.getTargetHash())).getBody());
        }

        @Override
        public void visit(SymbolicLink node) throws IOException {
            Files.createSymbolicLink(folder.resolve(node.getName()),
                    Path.of(new String(Blob.parse(readObject(node.getTargetHash())).getBody(),
                                       StandardCharsets.UTF_8)));
        }
    }

    private void writeTree(Tree tree) throws IOException {
        tree.accept(new TreeWriter(folder));
    }

    private static class Deleter extends SimpleFileVisitor<Path> {
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

    public void checkout(String encodedCommitHash) throws IOException {
        byte[] treeHash = Commit.extractTreeHash(readObject(encodedCommitHash));
        Files.walkFileTree(folder, new Deleter());
        writeTree(Tree.parse(readObject(treeHash)));
        writeReference("HEAD", new ReferenceContent(encodedCommitHash));
    }
}
