package record;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CommitTest {
    private static Commit initialCommit = null;

    @BeforeAll
    public static void setUp() {
        User user = new User("Jane Doe", "jane@example.com");
        Timestamp time = new Timestamp(ZonedDateTime.parse("2020-09-08T14:39:49+02:00"));
        initialCommit = new Commit(
            new Tree(
                new Node[] {
                    new File(false, "a", new Blob("a\n".getBytes(StandardCharsets.UTF_8))),
                    new File(false, "b", new Blob("b\n".getBytes(StandardCharsets.UTF_8)))
                }
            ),
            new Commit[0],
            user,
            time,
            user,
            time,
            "Initial commit"
        );
    }

    @Test
    void initialCommitGetBytes() {
        Assertions.assertArrayEquals(
            new byte[] {
                99,  111,  109,  109,  105,  116,   32,   49,
                54,   57,    0,  116,  114,  101,  101,   32,
                51,   54,   56,   51,  102,   56,   55,   48,
                98,  101,   52,   52,   54,   99,   55,   99,
                99,   48,   53,  102,  102,   97,  101,  102,
                57,  102,   97,   48,   54,   52,   49,   53,
                50,   55,   54,  101,   49,   56,   50,   56,
                10,   97,  117,  116,  104,  111,  114,   32,
                74,   97,  110,  101,   32,   68,  111,  101,
                32,   60,  106,   97,  110,  101,   64,  101,
               120,   97,  109,  112,  108,  101,   46,   99,
               111,  109,   62,   32,   49,   53,   57,   57,
                53,   54,   56,   55,   56,   57,   32,   43,
                48,   50,   48,   48,   10,   99,  111,  109,
               109,  105,  116,  116,  101,  114,   32,   74,
                97,  110,  101,   32,   68,  111,  101,   32,
                60,  106,   97,  110,  101,   64,  101,  120,
                97,  109,  112,  108,  101,   46,   99,  111,
               109,   62,   32,   49,   53,   57,   57,   53,
                54,   56,   55,   56,   57,   32,   43,   48,
                50,   48,   48,   10,   10,   73,  110,  105,
               116,  105,   97,  108,   32,   99,  111,  109,
               109,  105,  116,   10             
            },
            initialCommit.getBytes());
    }
}
