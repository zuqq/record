package record;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class SymbolicLink implements TreeNode {
    private final String name;
    private final byte[] blob;

    public SymbolicLink(String name, byte[] blob) {
        this.name = name;
        this.blob = blob;
    }

    @Override
    public String getMode() {
        return "120000";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public byte[] getTargetHash() {
        return blob;
    }
}
