package record;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TreeTests {
    private final Tree tree = new Tree(
        new Node[] {
            new File(
                false, "a", new Blob("a\n".getBytes(StandardCharsets.UTF_8))
            ),
            new File(
                false, "b", new Blob("test\n".getBytes(StandardCharsets.UTF_8))
            )

        }
    );

    @Test
    void getBytes() {
        Assertions.assertArrayEquals(
            tree.getBytes(),
            new byte[] {
                 116,  114,  101,  101,   32,   53,   56,    0,
                  49,   48,   48,   54,   52,   52,   32,   97,
                   0,  120, -104,   25,   34,   97,   59,   42,
                  -5,   96,   37,    4,   47,  -10,  -67, -121,
                -118,  -63, -103,   78, -123,   49,   48,   48,
                  54,   52,   52,   32,   98,    0,  -99,  -82,
                 -81,  -71, -122,   76,  -12,   48,   85,  -82,
                -109,  -66,  -80,  -81,  -42,  -57,  -47,   68,
                 -65,  -92
            }
        );
    }
}
