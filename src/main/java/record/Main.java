package record;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    private static void usage() {
        System.err.print("Usage:");
        System.err.println("\trecord init");
        System.err.println("\trecord commit NAME E-MAIL MESSAGE");
        System.err.println("\trecord checkout COMMIT");
        System.exit(-1);
    }

    public static void main(String... args) throws IOException {
        Repository repository = new Repository(Paths.get(".").toAbsolutePath().normalize());
        if (args.length == 1 && args[0].equals("init")) {
            repository.init();
        } else if (args.length == 4 && args[0].equals("commit")) {
            repository.commit(new User(args[1], args[2]), args[3]);
        } else if (args.length == 2 && args[0].equals("checkout")) {
            repository.checkout(args[1]);
        } else {
            usage();
        }
    }
}
