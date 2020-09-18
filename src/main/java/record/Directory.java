package record;

public final class Directory implements TreeNode {
    private final String name;
    private final LooseObjectReference<Tree> tree;

    public Directory(String name, LooseObjectReference<Tree> tree) {
        this.name = name;
        this.tree = tree;
    }

    @Override
    public Mode getMode() {
        return Mode.DIRECTORY;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public byte[] getTargetHash() {
        return tree.getTargetHash();
    }
}
