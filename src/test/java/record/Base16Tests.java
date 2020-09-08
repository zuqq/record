package record;

import java.text.ParseException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Base16Tests {
    private byte[] bytes = {
          97,   13,   81,   65,   24,  110,  -25,   42,   72,   23,
          -7,  -96,   96,   79,  -55,  -21,   34,  -45,  -22,   98
    };
    private String string = "610d5141186ee72a4817f9a0604fc9eb22d3ea62";

    @Test
    void encode() {
        Assertions.assertEquals(string, Base16.encode(bytes));
    }

    @Test
    void decode() {
        try {
            Assertions.assertArrayEquals(bytes, Base16.decode(string));
        } catch (ParseException e) {
            Assertions.fail("Parser failed on valid input.");
        }
    }

}