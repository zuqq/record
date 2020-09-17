package record;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {
    private static final String usage = "Usage:\n"
                + "\trecord init\n"
                + "\trecord commit NAME E-MAIL MESSAGE\n";

    public static void main(String[] args) throws IOException {
        Path folder = Paths.get(".").toAbsolutePath().normalize();
        Repository repository = new Repository(folder);
        if (args.length == 1 && args[0].equals("init")) {
            repository.init();
        } else if (args.length == 4 && args[0].equals("commit")) {
            repository.commit(new User(args[1], args[2]), args[3]);
        } else {
            System.out.println(usage);
        }
    }
}
