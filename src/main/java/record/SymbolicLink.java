package record;

/**
 * A tree node representing a symbolic link.
 */
public final class SymbolicLink implements TreeNode {
    private final String name;
    private final byte[] blob;

    public SymbolicLink(String name, byte[] blob) {
        this.name = name;
        this.blob = blob;
    }

    @Override
    public int getType() {
        return TreeNodeType.SYMBOLIC_LINK.getBits();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public byte[] getObjectHash() {
        return blob;
    }

    @Override
    public <E extends Exception> void accept(TreeVisitor<E> visitor) throws E {
        visitor.visit(this);
    }
}
