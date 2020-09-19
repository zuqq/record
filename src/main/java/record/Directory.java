package record;

public final class Directory implements TreeNode {
    private final String name;
    private final byte[] tree;

    public Directory(String name, byte[] tree) {
        this.name = name;
        this.tree = tree;
    }

    @Override
    public String getMode() {
        return "40000";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public byte[] getTargetHash() {
        return tree;
    }
}
