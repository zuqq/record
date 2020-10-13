package record;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    private static void usage() {
        System.err.print("Usage:");
        System.err.println("\trecord init");
        System.err.println("\trecord commit -m MESSAGE");
        System.err.println("\trecord checkout COMMIT");
        System.exit(-1);
    }

    /**
     * Traverses the file system upwards from the given directory until it
     * finds one that has a {@code .git} subfolder.
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
        Path directory = Paths.get(".").toRealPath();
        if (args.length == 1 && args[0].equals("init")) {
            new Repository(directory).init();
        } else if (args.length == 3 && args[0].equals("commit") && args[1].equals("-m")) {
            String name = System.getenv("GIT_COMMITTER_NAME");
            String email = System.getenv("GIT_COMMITTER_EMAIL");
            if (name == null || email == null) {
                throw new RuntimeException("Need GIT_COMMITTER_NAME and GIT_COMMITTER_EMAIL set.");
            }
            new Repository(findWorkingDirectory(directory)).commit(new User(name, email), args[2]);
        } else if (args.length == 2 && args[0].equals("checkout")) {
            new Repository(findWorkingDirectory(directory)).checkout(args[1]);
        } else {
            usage();
        }
    }
}
