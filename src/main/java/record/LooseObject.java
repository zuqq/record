package record;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public interface LooseObject {
    String getTag();

    byte[] getBody();

    default byte[] getBytes() {
        byte[] body = getBody();
        byte[] header = String
            .format("%s %d\0", getTag(), body.length)
            .getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[header.length + body.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(body, 0, result, header.length, body.length);
        return result;
    }

    default byte[] getHash() {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 is broken!");
        }
        return md.digest(getBytes());
    }
}
