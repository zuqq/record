package record;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public interface LooseObject {
    public abstract String getTag();

    public abstract byte[] getBody();

    public default byte[] getBytes() {
        byte[] body = getBody();
        byte[] header = String
            .format("%s %d\0", getTag(), body.length)
            .getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[header.length + body.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(body, 0, result, header.length, body.length);
        return result;
    }

    public default byte[] getHash() {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 is broken!");
        }
        return md.digest(getBytes());
    }
}
