package record;

public class Executable implements TreeNode {
    private final String name;
    private final LooseObjectReference<Blob> blob;

    public Executable(String name, LooseObjectReference<Blob> blob) {
        this.name = name;
        this.blob = blob;
    }

    @Override
    public Mode getMode() {
        return Mode.EXECUTABLE;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public byte[] getTargetHash() {
        return blob.getTargetHash();
    }
}
