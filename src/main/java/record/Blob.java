package record;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Arrays;

public class Blob {
    private final byte[] data;

    public Blob(byte[] data) {
        this.data = data;
    }

    public static Blob parse(byte[] input) throws ParseException {
        int i = 0;
        for (; i < input.length; ++i) {
            if (input[i] == 0) {
                break;
            }
        }
        String header = new String(
            Arrays.copyOfRange(input, 0, i),
            StandardCharsets.UTF_8
        );
        if (!header.startsWith("blob ")) {
            throw new ParseException("Malformed header.", 0);
        }
        byte[] data = Arrays.copyOfRange(input, i + 1, input.length);
        if (Integer.parseInt(header.substring(5)) != data.length) {
            throw new ParseException("Header contains incorrect length.", 5);
        }
        return new Blob(data);
    }

    public byte[] getBytes() {
        byte[] header = String
            .format("blob %d\0", data.length)
            .getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[header.length + data.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(data, 0, result, header.length, data.length);
        return result;
    }

    public byte[] getHash() {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 is broken!");
        }
        return md.digest(getBytes());
    }
}
