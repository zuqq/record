package record;

public enum TreeNodeType {
    DIRECTORY(0040000),
    EXECUTABLE(0100755),
    FILE(0100644),
    SYMBOLIC_LINK(0120000);

    private final int bits;

    TreeNodeType(int bits) {
        this.bits = bits;
    }

    public static TreeNodeType parse(int bits) {
        for (TreeNodeType type : values()) {
            if (bits == type.getBits()) {
                return type;
            }
        }
        throw new IllegalArgumentException("Illegal tree node type.");
    }

    public int getBits() {
        return bits;
    }
}
