package record;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    private static void usage() {
        System.err.print("Usage:");
        System.err.println("\trecord init");
        System.err.println("\trecord commit -m MESSAGE");
        System.err.println("\trecord checkout COMMIT");
        System.exit(-1);
    }

    public static void main(String... args) throws IOException {
        Repository repository = new Repository(Paths.get(".").toAbsolutePath().normalize());
        if (args.length == 1 && args[0].equals("init")) {
            repository.init();
        } else if (args.length == 3 && args[0].equals("commit") && args[1].equals("-m")) {
            String name = System.getenv("GIT_COMMITTER_NAME");
            String email = System.getenv("GIT_COMMITTER_EMAIL");
            if (name == null || email == null) {
                throw new RuntimeException("Need GIT_COMMITTER_NAME and GIT_COMMITTER_EMAIL set.");
            }
            repository.commit(new User(name, email), args[2]);
        } else if (args.length == 2 && args[0].equals("checkout")) {
            repository.checkout(args[1]);
        } else {
            usage();
        }
    }
}
