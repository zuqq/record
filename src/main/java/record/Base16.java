package record;

import java.text.ParseException;

public final class Base16 {
    private static String digits = "0123456789abcdef";

    public static String encode(byte[] data) {
        char[] result = new char[2 * data.length];
        for (int i = 0; i < data.length; ++i) {
            result[2 * i] = digits.charAt((data[i] & 0xf0) >>> 4);
            result[2 * i + 1] = digits.charAt(data[i] & 0x0f);
        }
        return String.valueOf(result);
    }

    public static byte[] decode(String string) throws ParseException {
        if (string.length() % 2 != 0) {
            throw new ParseException("Input is of odd length.", 0);
        }
        byte[] result = new byte[string.length() / 2];
        for (int i = 0; i < result.length; ++i) {
            int x = digits.indexOf(string.charAt(2 * i));
            int y = digits.indexOf(string.charAt(2 * i + 1));
            if (x == -1 || y == -1) {
                throw new ParseException("Not a hexadecimal byte.", 2 * i);
            }
            result[i] = (byte) ((x << 4) + y);
        }
        return result;
    }
}
