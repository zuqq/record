package record;

/**
 * A tree node representing a directory.
 */
public final class Directory implements TreeNode {
    private final String name;
    private final byte[] tree;

    public Directory(String name, byte[] tree) {
        this.name = name;
        this.tree = tree;
    }

    @Override
    public int getType() {
        return TreeNodeType.DIRECTORY.getBits();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public byte[] getObjectHash() {
        return tree;
    }

    @Override
    public <E extends Exception> void accept(TreeVisitor<E> visitor) throws E {
        visitor.visit(this);
    }
}
