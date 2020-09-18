package record;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class SymbolicLink implements TreeNode {
    private final String name;
    private final LooseObjectReference<Blob> blob;

    public SymbolicLink(String name, LooseObjectReference<Blob> blob) {
        this.name = name;
        this.blob = blob;
    }

    @Override
    public Mode getMode() {
        return Mode.SYMBOLIC_LINK;
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
