package record;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Arrays;

public final class Blob implements LooseObject {
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
        String header = new String(Arrays.copyOfRange(input, 0, i), StandardCharsets.UTF_8);
        if (!header.startsWith("blob ")) {
            throw new ParseException("Malformed header.", 0);
        }
        byte[] data = Arrays.copyOfRange(input, i + 1, input.length);
        if (Integer.parseInt(header.substring(5)) != data.length) {
            throw new ParseException("Header contains incorrect length.", 5);
        }
        return new Blob(data);
    }

    @Override
    public String getTag() {
        return "blob";
    }

    @Override
    public byte[] getBody() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Blob)) {
            return false;
        }
        Blob other = (Blob) o;
        return Arrays.equals(other.data, data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
