package record;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Loose git objects.
 *
 * The content of a loose git object is of the form
 *
 * <pre>{@code
 *     +------+----+-------------+-----+------+
 *     | type | SP | body length | NUL | body |
 *     +------+----+-------------+-----+------+
 * }</pre>
 *
 * where {@code type} is a string representing the object's type (e.g., {@code "blob"})
 * and {@code body length} is the decimal representation of {@code body}'s length.
 */
public interface LooseObject {
    /**
     * Get the object's type.
     *
     * @return A string representing the object's type.
     */
    String getType();

    /**
     * Get the body of the object's content.
     *
     * @return A byte array containing the body of the object's content.
     */
    byte[] getBody();

    /**
     * Get the object's uncompressed content.
     *
     * @return A byte array containing the object's content.
     */
    default byte[] getBytes() {
        byte[] body = getBody();
        byte[] header = String
                .format("%s %d\0", getType(), body.length)
                .getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[header.length + body.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(body, 0, result, header.length, body.length);
        return result;
    }

    /**
     * Get the object's hash.
     *
     * @return A byte array containing the SHA-1 digest of the object's content.
     */
    default byte[] getHash() {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 is broken!");
        }
        return md.digest(getBytes());
    }
}
