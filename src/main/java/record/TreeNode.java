package record;

import java.nio.charset.StandardCharsets;

public interface TreeNode {
    public abstract String getMode();

    public abstract String getName();

    public abstract byte[] getHash();

    public default byte[] toEntry() {
        byte[] part = String
            .format("%s %s\0", getMode(), getName())
            .getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[part.length + 20];
        System.arraycopy(part, 0, result, 0, part.length);
        byte[] hash = getHash();
        System.arraycopy(hash, 0, result, part.length, hash.length);
        return result;
    }
}
