package record;

public final class Directory implements TreeNode {
    private final String name;
    private final LooseObjectReference<Tree> treeReference;

    public Directory(String name, LooseObjectReference<Tree> treeReference) {
        this.name = name;
        this.treeReference = treeReference;
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
    public byte[] getTargetHash() {
        return treeReference.getTargetHash();
    }
}
