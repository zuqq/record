package record;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TreeTest {
    private static final Tree tree = new Tree(
        Arrays.asList(
            new File("a", new LooseObjectReference<>("78981922613b2afb6025042ff6bd878ac1994e85")),
            new File("b", new LooseObjectReference<>("61780798228d17af2d34fce4cfbdf35556832472"))
        )
    );

    @Test
    void getHash() {
        Assertions.assertEquals(
            "3683f870be446c7cc05ffaef9fa06415276e1828", Base16.encode(tree.getHash())
        );
    }
}
