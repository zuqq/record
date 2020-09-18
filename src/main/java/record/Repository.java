package record;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;

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
        private LooseObjectReference<Tree> result = null;

        public TreeBuilder(Path target) {
            this.target = target;
        }

        public LooseObjectReference<Tree> getResult() {
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
            LooseObjectReference<Tree> treeReference = new LooseObjectReference<>(tree);
            store.put(dir, new Directory(dir.getFileName().toString(), treeReference));
            if (dir.equals(target)) {
                result = treeReference;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (!Files.isHidden(file)) {
                TreeNode node;
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
                        new LooseObjectReference<>(blob)
                    );
                }
                store.put(file, node);
            }
            return FileVisitResult.CONTINUE;
        }
    }

    private LooseObjectReference<Tree> readTree() throws IOException {
        TreeBuilder visitor = new TreeBuilder(folder);
        Files.walkFileTree(folder, visitor);
        return visitor.getResult();
    }

    public void commit(User user, String message) throws IOException {
        String head = resolveReference("HEAD");
        List<LooseObjectReference<Commit>> parents = new ArrayList<>();
        if (Files.exists(git.resolve(head))) {
            parents.add(new LooseObjectReference<>(readReference(head).getTarget()));
        }
        Timestamp timestamp = new Timestamp(ZonedDateTime.now());
        Commit commit = new Commit(readTree(), parents, user, timestamp, user, timestamp, message);
        writeObject(commit);
        LooseObjectReference<Commit> commitReference = new LooseObjectReference<>(commit);
        writeReference(head, new ReferenceContent(commitReference.toString()));
    }
}
