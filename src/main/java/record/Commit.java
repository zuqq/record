package record;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * A git commit object.
 */
public final class Commit implements LooseObject {
    private final String tree;
    private final List<String> parents;
    private final User author;
    private final Timestamp authorDate;
    private final User committer;
    private final Timestamp committerDate;
    private final String message;

    public Commit(String tree,
                  List<String> parents,
                  User author,
                  Timestamp authorDate,
                  User committer,
                  Timestamp committerDate,
                  String message) {
        this.tree = tree;
        this.parents = parents;
        this.author = author;
        this.authorDate = authorDate;
        this.committer = committer;
        this.committerDate = committerDate;
        this.message = message;
    }

    public static byte[] extractTreeHash(byte[] input) throws FatalParseException {
        int i = FirstZero.in(input);
        String header = new String(input, 0, i, StandardCharsets.UTF_8);
        if (!header.startsWith("commit ")) {
            throw new FatalParseException("Malformed header.");
        }
        // Move to the start of the body.
        ++i;
        if (Integer.parseInt(header.substring(7)) != input.length - i) {
            throw new FatalParseException("Header contains incorrect length.");
        }
        // Skip past "tree ".
        i += 5;
        for (int j = i; j < input.length; ++j) {
            if (input[j] == '\n') {
                if (j - i != 40) {
                    throw new FatalParseException("Invalid tree hash.");
                }
                return Base16.decode(new String(input, i, j - i, StandardCharsets.UTF_8));
            }
        }
        throw new FatalParseException("Malformed body.");
    }

    @Override
    public String getTag() {
        return "commit";
    }

    @Override
    public byte[] getBody() {
        // Unlike trees, commits use Base16-encoded hashes of the objects they refer to.
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("tree ").append(tree).append('\n');
        for (String parent : parents) {
            bodyBuilder.append("parent ").append(parent).append('\n');
        }
        bodyBuilder
                .append("author ").append(author).append(' ').append(authorDate).append('\n')
                .append("committer ").append(committer).append(' ').append(committerDate).append('\n')
                .append('\n')
                .append(message).append('\n');
        return bodyBuilder.toString().getBytes(StandardCharsets.UTF_8);
    }
}
