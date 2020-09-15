package record;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.DeflaterOutputStream;

public final class Repository {
    Path folder;
    User user;  // TODO: Read this from ".git/config" or the system-wide configuration.

    public Repository(Path folder, User user) {
        this.folder = folder;
        this.user = user;
    }

    public void init() throws IOException {
        Path git = folder.resolve(".git");
        if (Files.exists(git)) {
            // TODO: Re-initialize the repository.
            throw new IllegalStateException("Repository already exists.");
        }
        // TODO: These paths deserve accessors.
        Files.createDirectories(git.resolve("objects"));
        Files.createDirectories(git.resolve("refs/heads"));
        Files.writeString(git.resolve("HEAD"), "ref: refs/heads/master\n");
    }

    private void writeObject(LooseObject object) throws IOException {
        String encodedHash = Base16.encode(object.getHash());
        Path bucket = folder.resolve(".git").resolve("objects").resolve(encodedHash.substring(0, 2));
        if (!Files.exists(bucket)) {
            Files.createDirectory(bucket);
        }
        try (
            OutputStream file = Files.newOutputStream(bucket.resolve(encodedHash.substring(2)));
            DeflaterOutputStream stream = new DeflaterOutputStream(file)
        ) {
            stream.write(object.getBytes());
            stream.flush();
        }
    }

    private class TreeBuilder extends SimpleFileVisitor<Path> {
        private final Path target;
        private final Map<Path, TreeNode> store = new HashMap<>();
        private ObjectReference<Tree> result = null;

        public TreeBuilder(Path target) {
            this.target = target;
        }

        public ObjectReference<Tree> getResult() {
            return result;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (Files.isHidden(dir)) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
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
            ObjectReference<Tree> treeReference = new ObjectReference<>(tree.getHash());
            store.put(dir, new Directory(dir.getFileName().toString(), treeReference));
            if (dir.equals(target)) {
                result = treeReference;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            TreeNode node = null;
            if (!Files.isHidden(file)) {
                if (Files.isSymbolicLink(file)) {
                    node = new SymbolicLink(
                        file.getFileName().toString(),
                        Files.readSymbolicLink(file)
                    );
                } else {
                    Blob blob = new Blob(Files.readAllBytes(file));
                    writeObject(blob);
                    node = new File(
                        Files.isExecutable(file),
                        file.getFileName().toString(),
                        new ObjectReference<>(blob.getHash())
                    );
                }
                store.put(file, node);
            }
            return FileVisitResult.CONTINUE;
        }
    }

    private ObjectReference<Tree> readTree() throws IOException {
        TreeBuilder visitor = new TreeBuilder(folder);
        Files.walkFileTree(folder, visitor);
        return visitor.getResult();
    }

    // Returns `Optional.empty()` if and only if HEAD points to an empty branch.
    private Optional<ObjectReference<Commit>> readHead() throws IOException {
        // TODO: Factor our commonalities of `readHead` and `writeHead`.
        String content = Files.readString(folder.resolve(".git/HEAD"));
        String encodedHash = null;
        if (content.startsWith("ref: ")) {
            // HEAD contains a symbolic reference.
            // TODO: Symbolic references deserve their own type.
            Path target = folder.resolve(".git").resolve(content.substring(5).stripTrailing());
            if (!Files.exists(target)) {
                return Optional.empty();
            }
            encodedHash = Files.readString(target);
        } else {
            // HEAD contains the Base16-encoded hash of a commit.
            encodedHash = content;
        }
        return Optional.of(new ObjectReference<>(Base16.decode(encodedHash)));
    }

    private void writeHead(ObjectReference<Commit> commitReference) throws IOException {
        String content = Files.readString(folder.resolve(".git/HEAD"));
        Path target = null;
        if (content.startsWith("ref: ")) {
            target = folder.resolve(".git").resolve(content.substring(5).stripTrailing());
        } else {
            target = folder.resolve(".git/HEAD");
        }
        Files.writeString(target, commitReference.toString() + "\n");
    }

    public void commit(String message) throws IOException {
        Timestamp timestamp = new Timestamp(ZonedDateTime.now());
        Commit commit = new Commit(
            readTree(),
            readHead().stream().collect(Collectors.toList()),
            user,
            timestamp,
            user,
            timestamp,
            message
        );
        writeObject(commit);
        writeHead(new ObjectReference<>(commit.getHash()));
    }
}
