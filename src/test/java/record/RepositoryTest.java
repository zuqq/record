package record;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class RepositoryTest {
    @Test
    void init() throws IOException {
        Path directory = Files.createTempDirectory("record");

        new Repository(directory).init();

        Assertions.assertEquals("ref: refs/heads/master\n",
                Files.readString(directory.resolve(".git/HEAD"), StandardCharsets.UTF_8));
    }

    @Nested
    class WithSetup {
        private Path directory;
        private Repository repository;

        @BeforeEach
        void setUp() throws IOException {
            directory = Files.createTempDirectory("record");
            repository = new Repository(directory);
            repository.init();
            Files.writeString(directory.resolve("a"), "a\n", StandardCharsets.UTF_8);
            Files.writeString(directory.resolve("b"), "b\n", StandardCharsets.UTF_8);
            repository.commit(new User("Jane Doe", "jane@example.com"),
                    Timestamp.of("1599568789 +0200"), "Initial commit");
        }

        @Test
        void commit() throws IOException {
            Assertions.assertEquals("42a22126b2d4fef6dd6537ecad0e63be1bc4c210\n",
                    Files.readString(directory.resolve(".git/refs/heads/master"), StandardCharsets.UTF_8));
        }

        @Test
        void branch() throws IOException {
            repository.branch("init");

            Assertions.assertEquals("42a22126b2d4fef6dd6537ecad0e63be1bc4c210\n",
                    Files.readString(directory.resolve(".git/refs/heads/init"), StandardCharsets.UTF_8));
        }

        @Test
        void checkout() throws IOException {
            repository.branch("init");
            Files.writeString(directory.resolve("c"), "more stuff\n", StandardCharsets.UTF_8);
            repository.commit(new User("Jane Doe", "jane@example.com"),
                    Timestamp.of("1599568810 +0200"), "Add more stuff");

            repository.checkout("init");

            Assertions.assertEquals("ref: refs/heads/init\n",
                    Files.readString(directory.resolve(".git/HEAD"), StandardCharsets.UTF_8));
            Assertions.assertFalse(Files.exists(directory.resolve("c")));
        }
    }
}
