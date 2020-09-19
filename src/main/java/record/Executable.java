package record;

public class Executable implements TreeNode {
    private final String name;
    private final byte[] blob;

    public Executable(String name, byte[] blob) {
        this.name = name;
        this.blob = blob;
    }

    @Override
    public String getMode() {
        return "100755";
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
