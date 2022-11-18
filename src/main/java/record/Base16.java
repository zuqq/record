package record;

import java.text.MessageFormat;

/**
 * Conversion to and from Base16.
 */
final class Base16 {
    private static final String digits = "0123456789abcdef";

    private Base16() {
        // No instantiation.
    }

    /**
     * Convert bytes into Base16.
     *
     * @param data An array of bytes.
     * @return The corresponding string of hexadecimal digits.
     */
    public static String encode(byte[] data) {
        char[] result = new char[2 * data.length];
        for (int i = 0; i < data.length; ++i) {
            result[2 * i] = digits.charAt((data[i] & 0xf0) >>> 4);
            result[2 * i + 1] = digits.charAt(data[i] & 0x0f);
        }
        return String.valueOf(result);
    }

    /**
     * Convert Base16 into bytes.
     *
     * @param string A string consisting of pairs of hexadecimal digits.
     * @return The corresponding array of bytes.
     * @throws FatalParseException If the input is malformed.
     */
    public static byte[] decode(String string) throws FatalParseException {
        if (string.length() % 2 != 0) {
            throw new FatalParseException("Input is of odd length.");
        }
        byte[] result = new byte[string.length() / 2];
        for (int i = 0; i < result.length; ++i) {
            int x = digits.indexOf(string.charAt(2 * i));
            int y = digits.indexOf(string.charAt(2 * i + 1));
            if (x == -1 || y == -1) {
                throw new FatalParseException(MessageFormat.format("Invalid hexadecimal byte at position {0}.", 2 * i));
            }
            result[i] = (byte) ((x << 4) + y);
        }
        return result;
    }
}
