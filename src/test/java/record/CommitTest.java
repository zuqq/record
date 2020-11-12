package record;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CommitTest {
    private final User user = new User("Jane Doe", "jane@example.com");
    private final Timestamp firstTimestamp = Timestamp.of("1599568789 +0200");
    private final Commit firstCommit = new Commit(
            "3683f870be446c7cc05ffaef9fa06415276e1828",
            new ArrayList<>(),
            user,
            firstTimestamp,
            user,
            firstTimestamp,
            "Initial commit");
    private final Timestamp secondTimestamp = Timestamp.of("1599568810 +0200");
    private final Commit secondCommit = new Commit(
            "5e1dd7430fe0d9b1678543ae1a318485d69fdd2c",
            Collections.singletonList(Base16.encode(firstCommit.getHash())),
            user,
            secondTimestamp,
            user,
            secondTimestamp,
            "Add more stuff");

    @Test
    void getHash() {
        Assertions.assertEquals("42a22126b2d4fef6dd6537ecad0e63be1bc4c210", Base16.encode(firstCommit.getHash()));
        Assertions.assertEquals("79d5cd29da3d56d9abda7e83d2b2bd52d43db939", Base16.encode(secondCommit.getHash()));
    }
}
