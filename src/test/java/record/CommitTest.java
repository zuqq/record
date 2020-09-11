package record;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CommitTest {
    private static Commit initialCommit = null;
    private static Commit secondCommit = null;

    @BeforeAll
    public static void setUp() {
        File a = new File(false, "a", new Blob("a\n".getBytes(StandardCharsets.UTF_8)));
        File b = new File(false, "b", new Blob("b\n".getBytes(StandardCharsets.UTF_8)));
        File c = new File(false, "c", new Blob("more stuff\n".getBytes(StandardCharsets.UTF_8)));
        User user = new User("Jane Doe", "jane@example.com");
        Timestamp initialTimestamp = new Timestamp(ZonedDateTime.parse("2020-09-08T14:39:49+02:00"));
        initialCommit = new Commit(
            new Tree(new TreeNode[] {a, b}),
            new Commit[] {},
            user,
            initialTimestamp,
            user,
            initialTimestamp,
            "Initial commit"
        );
        Timestamp secondTimestamp = new Timestamp(ZonedDateTime.parse("2020-09-08T14:40:10+02:00"));
        secondCommit = new Commit(
            new Tree(new TreeNode[] {a, b, c}),
            new Commit[] {initialCommit},
            user,
            secondTimestamp,
            user,
            secondTimestamp,
            "Add more stuff"
        );
    }

    @Test
    void initialCommitGetHash() {
        Assertions.assertEquals(
            "42a22126b2d4fef6dd6537ecad0e63be1bc4c210", Base16.encode(initialCommit.getHash())
        );
    }

    @Test
    void secondCommitGetHash() {
        Assertions.assertEquals(
            "79d5cd29da3d56d9abda7e83d2b2bd52d43db939", Base16.encode(secondCommit.getHash())
        );
    }
}
