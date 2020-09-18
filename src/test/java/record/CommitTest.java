package record;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CommitTest {
    private static Commit initialCommit = null;
    private static Commit secondCommit = null;

    @BeforeAll
    public static void setUp() {
        User user = new User("Jane Doe", "jane@example.com");
        Timestamp initialTimestamp = Timestamp.of("1599568789 +0200");
        initialCommit = new Commit(
            new LooseObjectReference<>("3683f870be446c7cc05ffaef9fa06415276e1828"),
            new ArrayList<>(),
            user,
            initialTimestamp,
            user,
            initialTimestamp,
            "Initial commit"
        );
        Timestamp secondTimestamp = Timestamp.of("1599568810 +0200");
        secondCommit = new Commit(
            new LooseObjectReference<>("5e1dd7430fe0d9b1678543ae1a318485d69fdd2c"),
            Collections.singletonList(new LooseObjectReference<>(initialCommit.getHash())),
            user,
            secondTimestamp,
            user,
            secondTimestamp,
            "Add more stuff"
        );
    }

    @Test
    void getHash() {
        Assertions.assertEquals(
            "42a22126b2d4fef6dd6537ecad0e63be1bc4c210", Base16.encode(initialCommit.getHash())
        );
        Assertions.assertEquals(
            "79d5cd29da3d56d9abda7e83d2b2bd52d43db939", Base16.encode(secondCommit.getHash())
        );
    }
}
