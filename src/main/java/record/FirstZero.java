package record;

/**
 * Find the first zero byte in a byte array.
 */
final class FirstZero {
    private FirstZero() {}

    /**
     * Returns the index of the first zero byte in the subarray of {@code input}
     * starting at {@code offset}.
     *
     * @param input A byte array.
     * @param offset An offset in the byte array.
     * @return The index of the first zero byte in the subarray of {@code input}
     *         starting at {@code offset}.
     * @throws FatalParseException If the subarray of {@code input} starting at
     *         {@code offset} contains no zero byte.
     */
    public static int in(byte[] input, int offset) throws FatalParseException {
        for (int i = offset; i < input.length; ++i) {
            if (input[i] == 0) {
                return i;
            }
        }
        throw new FatalParseException("No zero!");
    }

    /**
     * Returns the index of the first zero byte in {@code input}.
     *
     * @param input A byte array.
     * @return The index of the first zero byte in {@code input}.
     * @throws FatalParseException If {@code input} contains no zero byte.
     */
    public static int in(byte[] input) throws FatalParseException {
        return in(input, 0);
    }
}
