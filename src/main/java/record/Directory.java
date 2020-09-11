package record;

public final class Directory implements TreeNode {
    private final String name;
    private final Tree tree;

    public Directory(String name, Tree tree) {
        this.name = name;
        this.tree = tree;
    }

    @Override
    public String getMode() {
        return "040000";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public byte[] getHash() {
        return tree.getHash();
    }
}
