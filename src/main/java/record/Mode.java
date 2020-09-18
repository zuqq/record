package record;

public enum Mode {
    FILE          ("100644"),
    EXECUTABLE    ("100755"),
    DIRECTORY     ("040000"),
    SYMBOLIC_LINK ("120000");

    private final String value;

    private Mode(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
