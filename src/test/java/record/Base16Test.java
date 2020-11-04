package record;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Base16Test {
    private final byte[] bytes = {
        (byte) 0x78, (byte) 0x98, (byte) 0x19, (byte) 0x22, (byte) 0x61,
        (byte) 0x3b, (byte) 0x2a, (byte) 0xfb, (byte) 0x60, (byte) 0x25,
        (byte) 0x04, (byte) 0x2f, (byte) 0xf6, (byte) 0xbd, (byte) 0x87,
        (byte) 0x8a, (byte) 0xc1, (byte) 0x99, (byte) 0x4e, (byte) 0x85
    };
    private final String string = "78981922613b2afb6025042ff6bd878ac1994e85";

    @Test
    void encode() {
        Assertions.assertEquals(string, Base16.encode(bytes));
    }

    @Test
    void decode() {
        try {
            Assertions.assertArrayEquals(bytes, Base16.decode(string));
        } catch (FatalParseException e) {
            Assertions.fail("Parser failed on valid input.");
        }

        Assertions.assertThrows(FatalParseException.class, () -> Base16.decode("a"));

        Assertions.assertThrows(FatalParseException.class, () -> Base16.decode("ax"));
        Assertions.assertThrows(FatalParseException.class, () -> Base16.decode("xa"));
    }
}