package record;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FirstZeroTest {
    @Test
    void in() {
        Assertions.assertEquals(1, FirstZero.in(new byte[] {1, 0, 1}));

        Assertions.assertThrows(FatalParseException.class, () -> FirstZero.in(new byte[] {}));
    }
}
