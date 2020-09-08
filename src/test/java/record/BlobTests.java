package record;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BlobTests {
    // https://git-scm.com/book/en/v2/Git-Internals-Git-Objects
    private final Blob blob = new Blob(
        "what is up, doc?".getBytes(StandardCharsets.UTF_8)
    );

    @Test
    void parse() {
        try {
            Assertions.assertEquals(Blob.parse(blob.getBytes()), blob);
        } catch (ParseException e) {
            Assertions.fail("Parser failed on valid input.", e);
        }
    }

    @Test
    void getBytes() {
        Assertions.assertArrayEquals(
            "blob 16\0what is up, doc?".getBytes(StandardCharsets.UTF_8),
            blob.getBytes()
        );
    }
    
    @Test
    void getHash() {
        Assertions.assertEquals(
            "bd9dbf5aae1a3862dd1526723246b20206e5fc37", Base16.encode(blob.getHash())
        );
    }
}
