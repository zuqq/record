package record;

public class FirstZero {
    /**
     * Returns the index of the first zero in the subarray of {@code input}
     * starting at {@code offset}; throws {@link FatalParseException} if it
     * doesn't contain zero.
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
     * Returns the index of the first zero in {@code input}; throws
     * {@link FatalParseException} if it doesn't contain zero.
     */
    public static int in(byte[] input) throws FatalParseException {
        return in(input, 0);
    }
}
