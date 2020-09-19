package record;

public final class File implements TreeNode {
    private final String name;
    // This uses a `LooseObjectReference` instead of just storing the hash
    // because that gives me greater type safety in `Repository::TreeBuilder`.
    private final byte[] blob;

    public File(String name, byte[] blob) {
        this.name = name;
        this.blob = blob;
    }

    @Override
    public String getMode() {
        return "100644";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public byte[] getTargetHash() {
        return blob;
    }

    @Override
    public <E extends Exception> void accept(TreeVisitor<E> visitor) throws E {
        visitor.visit(this);
    }
}
