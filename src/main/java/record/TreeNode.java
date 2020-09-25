package record;

import java.nio.charset.StandardCharsets;

/**
 * A node in a {@link Tree}, i.e., a named {@link LooseObject}. It is serialized
 * according to the following schema:
 *
 * <pre><code>
 * +------+----+------+-----+------+
 * | type | SP | name | NUL | hash |
 * +------+----+------+-----+------+
 * </code></pre>
 *
 * where {@code type} is an octal representation of node's type, {@code name} is
 * a string not containing the null byte, and {@code hash} is the hash of the
 * underlying {@link LooseObject}.
 */
public interface TreeNode {
    /**
     * Get the node's type.
     *
     * @return An integer representing the node's type.
     */
    int getType();

    /**
     * Get the node's name.
     *
     * @return A string representing the node's name.
     */
    String getName();

    /**
     * Get the {@link LooseObject}'s hash.
     *
     * @return A byte array containing the SHA-1 digest of the underlying
     *         {@link LooseObject}'s content.
     */
    byte[] getTargetHash();

    /**
     * Serialize the node.
     *
     * @return A byte array containing the node's serialization.
     */
    default byte[] toEntry() {
        byte[] part = String
                .format("%o %s\0", getType(), getName())
                .getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[part.length + 20];
        System.arraycopy(part, 0, result, 0, part.length);
        byte[] hash = getTargetHash();
        System.arraycopy(hash, 0, result, part.length, hash.length);
        return result;
    }

    <E extends Exception> void accept(TreeVisitor<E> visitor) throws E;
}
