package record;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    private static void usage() {
        System.err.print("Usage:");
        System.err.println("\trecord init");
        System.err.println("\trecord commit -m <message>");
        System.err.println("\trecord branch <branch>");
        System.err.println("\trecord checkout <branch or commit>");
        System.exit(-1);
    }

    /**
     * Finds the closest ancestor of {@code directory} that has a {@code .git} subdirectory.
     *
     * @throws IOException If none of the ancestors has a {@code .git} subdirectory.
     */
    private static Path findWorkingDirectory(Path directory) throws IOException {
        while (directory != null) {
            if (Files.isDirectory(directory.resolve(".git"))) {
                return directory;
            }
            directory = directory.getParent();
        }
        throw new RuntimeException("No .git repository found.");
    }

    public static void main(String... args) throws IOException {
        Path directory = Path.of(".").toRealPath();
        if (args.length == 1 && args[0].equals("init")) {
            new Repository(directory).init();
        } else if (args.length == 3 && args[0].equals("commit") && args[1].equals("-m")) {
            String name = System.getenv("GIT_COMMITTER_NAME");
            String email = System.getenv("GIT_COMMITTER_EMAIL");
            if (name == null || email == null) {
                throw new RuntimeException("Need GIT_COMMITTER_NAME and GIT_COMMITTER_EMAIL set.");
            }
            new Repository(findWorkingDirectory(directory)).commit(new User(name, email), Timestamp.now(), args[2]);
        } else if (args.length == 2 && args[0].equals("branch")) {
            new Repository(findWorkingDirectory(directory)).branch(args[1]);
        } else if (args.length == 2 && args[0].equals("checkout")) {
            new Repository(findWorkingDirectory(directory)).checkout(args[1]);
        } else {
            usage();
        }
    }
}
