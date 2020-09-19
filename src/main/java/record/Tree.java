package record;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * A git tree object.
 *
 * Trees are (anonymous) snapshots of directories. Because a directory's
 * children have names, we can't use {@link Blob} and {@link Tree} as
 * tree nodes. Instead, there is a separate interface {@link TreeNode}.
 */
public final class Tree implements LooseObject {
    private final List<TreeNode> children;

    public Tree(List<TreeNode> children) {
        children.sort(Comparator.comparing(TreeNode::getName));
        this.children = children;
    }

    public static Tree parse(byte[] input) {
        int i = FirstZero.in(input);
        String header = new String(Arrays.copyOfRange(input, 0, i), StandardCharsets.UTF_8);
        if (!header.startsWith("tree ")) {
            throw new FatalParseException("Malformed header.");
        }
        // Move `i` to the start of the body.
        ++i;
        if (Integer.parseInt(header.substring(5)) != input.length - i) {
            throw new FatalParseException("Header contains incorrect length.");
        }
        List<TreeNode> children = new ArrayList<>();
        while (i < input.length) {
            int j = FirstZero.in(input, i);
            // It's `j - i` instead of `j - i + 1` because zero doesn't belong.
            String prefix = new String(input, i, j - i, StandardCharsets.UTF_8);
            int spaceIndex = prefix.indexOf(' ');
            String name = prefix.substring(spaceIndex + 1);
            // Move `i` to the start of the next entry.
            i = j + 21;
            byte[] hash = Arrays.copyOfRange(input, j + 1, i);
            TreeNode child = switch (prefix.substring(0, spaceIndex)) {
                case "40000"  -> new Directory(name, hash);
                case "100755" -> new Executable(name, hash);
                case "100644" -> new File(name, hash);
                case "120000" -> new SymbolicLink(name, hash);
                default       -> throw new FatalParseException("Illegal mode.");
            };
            children.add(child);
        }
        return new Tree(children);
    }

    @Override
    public String getTag() {
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
}
