package record;

/**
 * A tree node representing a file.
 */
public final class File implements TreeNode {
    private final String name;
    private final boolean executable;
    private final byte[] blob;

    public File(String name, boolean executable, byte[] blob) {
        this.name = name;
        this.executable = executable;
        this.blob = blob;
    }

    public boolean isExecutable() {
        return executable;
    }

    @Override
    public TreeNodeType getType() {
        return executable ? TreeNodeType.EXECUTABLE : TreeNodeType.FILE;
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
    public <E extends Exception> void accept(TreeNodeVisitor<E> visitor) throws E {
        visitor.visit(this);
    }
}
