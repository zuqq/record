package record;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TreeTest {
    private final Tree tree = new Tree(
        new Node[] {
            new File(false, "a", new Blob("a\n".getBytes(StandardCharsets.UTF_8))),
            new File(false, "b", new Blob("b\n".getBytes(StandardCharsets.UTF_8)))
        }
    );

    @Test
    void getHash() {
        Assertions.assertEquals(
            "3683f870be446c7cc05ffaef9fa06415276e1828", Base16.encode(tree.getHash())
        );
    }
}
