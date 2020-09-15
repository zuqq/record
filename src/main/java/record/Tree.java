package record;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class Tree implements LooseObject {
    private final List<TreeNode> children;

    public Tree(List<TreeNode> children) {
        children.sort(Comparator.comparing(TreeNode::getName));
        this.children = children;
    }

    public List<TreeNode> getChildren() {
        return children;
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
