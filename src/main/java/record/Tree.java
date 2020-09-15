package record;

import java.util.Arrays;
import java.util.Comparator;

public final class Tree implements LooseObject {
    private final TreeNode[] children;

    public Tree(TreeNode[] children) {
        Arrays.sort(children, Comparator.comparing(TreeNode::getName));
        this.children = children;
    }

    public TreeNode[] getChildren() {
        return children;
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
