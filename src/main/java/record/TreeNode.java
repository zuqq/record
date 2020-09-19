package record;

import java.nio.charset.StandardCharsets;

/**
 * A node in a {@link Tree}.
 */
public interface TreeNode {
    String getMode();

    String getName();

    byte[] getTargetHash();

    default byte[] toEntry() {
        byte[] part = String
                .format("%s %s\0", getMode(), getName())
                .getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[part.length + 20];
        System.arraycopy(part, 0, result, 0, part.length);
        byte[] hash = getTargetHash();
        System.arraycopy(hash, 0, result, part.length, hash.length);
        return result;
    }
}
