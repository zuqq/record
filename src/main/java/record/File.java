package record;

public final class File implements TreeNode {
    private final boolean executable;
    private final String name;
    private final ObjectReference blobReference;

    public File(boolean executable, String name, ObjectReference blobReference) {
        this.executable = executable;
        this.name = name;
        this.blobReference = blobReference;
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
        return blobReference.getTargetHash();
    }
}
