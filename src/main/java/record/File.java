package record;

public final class File implements TreeNode {
    private final boolean executable;
    private final String name;
    // This uses a `LooseObjectReference` instead of just storing the hash
    // because that gives me greater type safety in `Repository::TreeBuilder`.
    private final LooseObjectReference<Blob> blob;

    public File(boolean executable, String name, LooseObjectReference<Blob> blob) {
        this.executable = executable;
        this.name = name;
        this.blob = blob;
    }

    @Override
    public String getMode() {
        if (executable) {
            return "100755";
        } else {
            return "100644";
        }
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
