package record;

public class File implements Node {
    private final boolean executable;
    private final String name;
    private final Blob blob;

    public File(boolean executable, String name, Blob blob) {
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
    public byte[] getHash() {
        return blob.getHash();
    }
}
