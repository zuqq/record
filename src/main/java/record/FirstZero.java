package record;

/**
 * Find the first zero byte in a byte array.
 */
final class FirstZero {
    private FirstZero() {
        // No instantiation.
    }

    /**
     * Returns the index of the first zero byte in {@code input} from {@code offset} on.
     *
     * @param input  A byte array.
     * @param offset An offset in the byte array.
     * @return The index of the first zero byte in {@code input} from {@code offset} on.
     * @throws FatalParseException If no such byte exists.
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
     * @throws FatalParseException If no such byte exists.
     */
    public static int in(byte[] input) throws FatalParseException {
        return in(input, 0);
    }
}
