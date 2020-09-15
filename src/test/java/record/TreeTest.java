package record;

import java.text.ParseException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TreeTest {
    private static Tree tree = null;

    @BeforeAll
    public static void setUp() {
        byte[] aHash = null;
        byte[] bHash = null;
        try {
            aHash = Base16.decode("78981922613b2afb6025042ff6bd878ac1994e85");
            bHash = Base16.decode("61780798228d17af2d34fce4cfbdf35556832472");
        } catch (ParseException e) {
            throw new RuntimeException("Your test is broken!");
        }
        tree = new Tree(
            new TreeNode[] {
                new File(false, "a", new ObjectReference(aHash)),
                new File(false, "b", new ObjectReference(bHash))
            }
        );
    }

    @Test
    void getHash() {
        Assertions.assertEquals(
            "3683f870be446c7cc05ffaef9fa06415276e1828", Base16.encode(tree.getHash())
        );
    }
}
