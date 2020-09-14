package record;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public final class Tree implements LooseObject {
    private static class TreeBuilder extends SimpleFileVisitor<Path> {
        private final Path target;
        private final Map<Path, TreeNode> store = new HashMap<Path, TreeNode>();
        private Tree result = null;

        public TreeBuilder(Path target) {
            this.target = target;
        }

        public Tree getResult() {
            return result;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc != null) {
                throw exc;
            }
            ArrayList<TreeNode> children = new ArrayList<TreeNode>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path child : stream) {
                    TreeNode node = store.get(child);
                    if (node == null) {
                        throw new RuntimeException("The algorithm is broken!");
                    }
                    children.add(node);
                    store.remove(child);
                }
            }
            Tree tree = new Tree(children.toArray(new TreeNode[0]));
            store.put(dir, new Directory(dir.getFileName().toString(), tree));
            if (dir.equals(target)) {
                result = tree;
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
                    node = new File(
                        Files.isExecutable(file),
                        file.getFileName().toString(),
                        new Blob(Files.readAllBytes(file))
                    );
                }
                store.put(file, node);
            }
            return FileVisitResult.CONTINUE;
        }
    }

    private static class TreeWriter implements TreeVisitor<IOException> {
        private Path folder;

        public TreeWriter(Path folder) {
            this.folder = folder;
        }

        @Override
        public void visitEnter(Directory node) throws IOException {
            folder = folder.resolve(node.getName());
            try {
                Files.createDirectory(folder);
            } catch (FileAlreadyExistsException e) {
                // No problem!
            }
        }

        @Override
        public void visitLeave(Directory node) {
            folder = folder.getParent();
        }

        @Override
        public void visit(File node) throws IOException {
            Files.write(folder.resolve(node.getName()), node.getBytes());
        }

        @Override
        public void visit(SymbolicLink node) throws IOException {
            Files.createSymbolicLink(
                folder.resolve(node.getName()),
                Path.of(new String(node.getBytes(), StandardCharsets.UTF_8))
            );
        }
    }

    private final TreeNode[] children;

    public Tree(TreeNode[] children) {
        Arrays.sort(children, Comparator.comparing(TreeNode::getName));
        this.children = children;
    }

    public TreeNode[] getChildren() {
        return children;
    }

    public static Tree of(Path folder) throws IOException {
        TreeBuilder visitor = new TreeBuilder(folder);
        Files.walkFileTree(folder, visitor);
        return visitor.getResult();
    }

    public void write(Path folder) throws IOException {
        TreeWriter visitor = new TreeWriter(folder);
        for (TreeNode child : children) {
            child.accept(visitor);
        }
    }

    @Override
    public String getTag() {
        return "tree";
    }

    @Override
    public byte[] getBody() {
        byte[][] entries = new byte[children.length][];
        int length = 0;
        for (int i = 0; i < children.length; ++i) {
            entries[i] = children[i].toEntry();
            length += entries[i].length;
        }
        byte[] result = new byte[length];
        int offset = 0;
        for (int i = 0; i < entries.length; ++i) {
            System.arraycopy(entries[i], 0, result, offset, entries[i].length);
            offset += entries[i].length;
        }
        return result;
    }
}
