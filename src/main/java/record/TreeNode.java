package record;

import java.nio.charset.StandardCharsets;

/**
 * A node in a {@link Tree}, i.e., a named reference to a {@link LooseObject}.
 *
 * <p>It is serialized by {@link #toEntry()} according to the following schema:
 * <pre><code>
 * +------+----+------+-----+------+
 * | type | SP | name | NUL | hash |
 * +------+----+------+-----+------+
 * </code></pre>
 * where {@code type} is an octal representation of node's type, {@code name} is
 * a string not containing the null byte, and {@code hash} is the hash of the
 * underlying {@link LooseObject}.
 */
public interface TreeNode {
    /**
     * Get the node's type.
     *
     * @return An instance of {@link TreeNodeType}.
     */
    TreeNodeType getType();

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
    byte[] getObjectHash();

    /**
     * Serialize the node.
     *
     * @return A byte array containing the node's serialization.
     */
    default byte[] toEntry() {
        byte[] part = String
            .format("%o %s\0", getType().getBits(), getName())
            .getBytes(StandardCharsets.UTF_8);
        byte[] hash = getObjectHash();
        byte[] result = new byte[part.length + hash.length];
        System.arraycopy(part, 0, result, 0, part.length);
        System.arraycopy(hash, 0, result, part.length, hash.length);
        return result;
    }

    <E extends Exception> void accept(TreeNodeVisitor<E> visitor) throws E;
}
