package record;

public final class File implements TreeNode {
    private final String name;
    // This uses a `LooseObjectReference` instead of just storing the hash
    // because that gives me greater type safety in `Repository::TreeBuilder`.
    private final LooseObjectReference<Blob> blob;

    public File(String name, LooseObjectReference<Blob> blob) {
        this.name = name;
        this.blob = blob;
    }

    @Override
    public Mode getMode() {
        return Mode.FILE;
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
