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

        Assertions.assertEquals(
                "ref: refs/heads/master\n",
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
            Files.createDirectory(directory.resolve("src"));
            Files.writeString(directory.resolve("src/a"), "a\n", StandardCharsets.UTF_8);
            repository.commit(
                    new User("Jane Doe", "jane@example.com"),
                    Timestamp.of("1604560870 +0100"),
                    "Initial commit");
        }

        @Test
        void commit() throws IOException {
            Assertions.assertEquals(
                    "3d55094ecc4dc83fccdeac612207d3f313b570ce\n",
                    Files.readString(directory.resolve(".git/refs/heads/master"), StandardCharsets.UTF_8));
        }

        @Test
        void branch() throws IOException {
            repository.branch("init");

            Assertions.assertEquals(
                    "3d55094ecc4dc83fccdeac612207d3f313b570ce\n",
                    Files.readString(directory.resolve(".git/refs/heads/init"), StandardCharsets.UTF_8));
        }

        @Test
        void checkout() throws IOException {
            Files.createDirectory(directory.resolve("x"));
            Files.writeString(directory.resolve("x/.x"), "x\n", StandardCharsets.UTF_8);
            Files.createSymbolicLink(directory.resolve("a"), directory.resolve("src/a"));
            repository.commit(
                    new User("Jane Doe", "jane@example.com"),
                    Timestamp.of("1604560898 +0100"),
                    "Add more stuff");

            repository.checkout("3d55094ecc4dc83fccdeac612207d3f313b570ce");

            Assertions.assertEquals(
                    "3d55094ecc4dc83fccdeac612207d3f313b570ce\n",
                    Files.readString(directory.resolve(".git/HEAD"), StandardCharsets.UTF_8));
            Assertions.assertEquals(
                    "x\n", Files.readString(directory.resolve("x/.x"), StandardCharsets.UTF_8));
            Assertions.assertFalse(Files.exists(directory.resolve("a")));

            repository.checkout("master");

            Assertions.assertEquals(
                    "ref: refs/heads/master\n",
                    Files.readString(directory.resolve(".git/HEAD"), StandardCharsets.UTF_8));
            Assertions.assertEquals(
                    directory.resolve("src/a"), Files.readSymbolicLink(directory.resolve("a")));
        }
    }
}
