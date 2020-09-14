package record;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class SymbolicLink implements TreeNode {
    private final String name;
    private final Blob blob;

    public SymbolicLink(String name, Path target) {
        this.name = name;
        this.blob = new Blob(target.toString().getBytes(StandardCharsets.UTF_8));
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
    public byte[] getHash() {
        return blob.getHash();
    }

    @Override
    public <E extends Exception> void accept(TreeVisitor<E> visitor) throws E {
        visitor.visit(this);
    }

    public byte[] getBytes() {
        return blob.getBody();
    }
}
