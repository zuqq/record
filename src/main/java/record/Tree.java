package record;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * A git tree object, i.e., a snapshot of a directory.
 *
 * A tree consists of a list of {@link TreeNode}s that represent the directory's entries.
 */
public final class Tree implements LooseObject {
    private final List<TreeNode> children;

    public Tree(List<TreeNode> children) {
        children.sort(Comparator.comparing(TreeNode::getName));
        this.children = children;
    }

    /**
     * Reconstruct a tree from its content.
     *
     * @param input A byte array containing the tree's content.
     * @return The corresponding {@link Tree}.
     * @throws FatalParseException If {@code input} is not a valid tree.
     */
    public static Tree parse(byte[] input) throws FatalParseException {
        int i = FirstZero.in(input);
        String header = new String(input, 0, i, StandardCharsets.UTF_8);
        if (!header.startsWith("tree ")) {
            throw new FatalParseException("Malformed header.");
        }
        // Move to the start of the body.
        ++i;
        if (Integer.parseInt(header.substring(5)) != input.length - i) {
            throw new FatalParseException("Header contains incorrect length.");
        }
        List<TreeNode> children = new ArrayList<>();
        while (i < input.length) {
            int j = FirstZero.in(input, i);
            String prefix = new String(input, i, j - i, StandardCharsets.UTF_8);
            int spaceIndex = prefix.indexOf(' ');
            String name = prefix.substring(spaceIndex + 1);
            // Move to the start of the next entry.
            i = j + 21;
            byte[] hash = Arrays.copyOfRange(input, j + 1, i);
            TreeNode child = switch (Integer.parseInt(prefix.substring(0, spaceIndex - 4), 8)) {
                case 004 -> new Directory(name, hash);
                case 010 -> switch (Integer.parseInt(prefix.substring(spaceIndex - 3, spaceIndex), 8)) {
                    case 0755 -> new File(name, true, hash);
                    case 0644 -> new File(name, false, hash);
                    default   -> throw new FatalParseException("Illegal file permissions.");
                };
                case 012 -> new SymbolicLink(name, hash);
                default  -> throw new FatalParseException("Illegal file type.");
            };
            children.add(child);
        }
        return new Tree(children);
    }

    @Override
    public String getType() {
        return "tree";
    }

    @Override
    public byte[] getBody() {
        List<byte[]> entries = new ArrayList<>();
        int length = 0;
        for (TreeNode child : children) {
            byte[] entry = child.toEntry();
            entries.add(entry);
            length += entry.length;
        }
        byte[] result = new byte[length];
        int offset = 0;
        for (byte[] entry : entries) {
            System.arraycopy(entry, 0, result, offset, entry.length);
            offset += entry.length;
        }
        return result;
    }

    public <E extends Exception> void accept(TreeVisitor<E> visitor) throws E {
        for (TreeNode child : children) {
            child.accept(visitor);
        }
    }
}
