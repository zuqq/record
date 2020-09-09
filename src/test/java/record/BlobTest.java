package record;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BlobTest {
    private final Blob blob = new Blob("a\n".getBytes(StandardCharsets.UTF_8));

    @Test
    void parse() {
        try {
            Assertions.assertEquals(Blob.parse(blob.getBytes()), blob);
        } catch (ParseException e) {
            Assertions.fail("Parser failed on valid input.", e);
        }
    }
    
    @Test
    void getHash() {
        Assertions.assertEquals(
            "78981922613b2afb6025042ff6bd878ac1994e85", Base16.encode(blob.getHash())
        );
    }
}
