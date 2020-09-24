package record;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * A git blob object.
 *
 * Blobs are (anonymous) snapshots of files.
 */
public final class Blob implements LooseObject {
    private final byte[] data;

    public Blob(byte[] data) {
        this.data = data;
    }

    public static Blob parse(byte[] input) throws FatalParseException {
        int i = FirstZero.in(input);
        String header = new String(Arrays.copyOfRange(input, 0, i), StandardCharsets.UTF_8);
        if (!header.startsWith("blob ")) {
            throw new FatalParseException("Malformed header.");
        }
        // Move to the start of the body.
        ++i;
        byte[] data = Arrays.copyOfRange(input, i, input.length);
        if (Integer.parseInt(header.substring(5)) != data.length) {
            throw new FatalParseException("Header contains incorrect length.");
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
