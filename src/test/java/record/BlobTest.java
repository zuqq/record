package record;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BlobTest {
    private final Blob a = new Blob("a\n".getBytes(StandardCharsets.UTF_8));
    private final Blob b = new Blob("b\n".getBytes(StandardCharsets.UTF_8));
    private final Blob c = new Blob("more stuff\n".getBytes(StandardCharsets.UTF_8));

    @Test
    void parse() {
        try {
            Assertions.assertEquals(Blob.parse(a.getBytes()), a);
        } catch (FatalParseException e) {
            Assertions.fail("Parser failed on valid input.", e);
        }
    }
    
    @Test
    void getHash() {
        Assertions.assertEquals(
            "78981922613b2afb6025042ff6bd878ac1994e85", Base16.encode(a.getHash())
        );
        Assertions.assertEquals(
            "61780798228d17af2d34fce4cfbdf35556832472", Base16.encode(b.getHash())
        );
        Assertions.assertEquals(
            "de8ed3a567a5e7f2f7eb99365f8b4e144a08ce77", Base16.encode(c.getHash())
        );
    }
}
