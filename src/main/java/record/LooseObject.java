package record;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public interface LooseObject {
    public abstract byte[] getBytes();

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
