package record;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BlobTests {
    // https://git-scm.com/book/en/v2/Git-Internals-Git-Objects
    private final Blob blob = new Blob(
        "what is up, doc?".getBytes(StandardCharsets.UTF_8)
    );

    @Test
    void getBytes() {
        Assertions.assertArrayEquals(
            blob.getBytes(),
            "blob 16\0what is up, doc?".getBytes(StandardCharsets.UTF_8)
        );
    }
    
    @Test
    void getHash() {
        StringBuilder builder = new StringBuilder();
        for (byte b : blob.getHash()) {
            builder.append(String.format("%02x", b));
        }
        Assertions.assertEquals(
            builder.toString(), "bd9dbf5aae1a3862dd1526723246b20206e5fc37"
        );
    }
}
