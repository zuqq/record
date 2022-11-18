package record;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * A Git commit object.
 *
 * <p>Commits are textual objects: unlike trees, they use Base16-encoded hashes
 * of the objects they refer to.
 */
public final class Commit implements LooseObject {
    private final String tree;
    private final List<String> parents;
    private final User author;
    private final Timestamp authorDate;
    private final User committer;
    private final Timestamp committerDate;
    private final String message;

    /**
     * @param tree          The tree's Base16-encoded hash.
     * @param parents       A list of the parents' Base16-encoded hashes.
     * @param author        Who created the content.
     * @param authorDate    When the content was created.
     * @param committer     Who is creating the commit.
     * @param committerDate When the commit is being created.
     * @param message       The commit message.
     */
    public Commit(
        String tree,
        List<String> parents,
        User author,
        Timestamp authorDate,
        User committer,
        Timestamp committerDate,
        String message
    ) {
        this.tree = tree;
        this.parents = parents;
        this.author = author;
        this.authorDate = authorDate;
        this.committer = committer;
        this.committerDate = committerDate;
        this.message = message;
    }

    /**
     * Extract the tree hash from a commit's content.
     *
     * @param input A byte array containing the commit's content.
     * @return The hash of the commit's tree.
     * @throws FatalParseException If {@code input} is not a valid commit.
     */
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
    public String getType() {
        return "commit";
    }

    @Override
    public byte[] getBody() {
        StringBuilder builder = new StringBuilder().append("tree ").append(tree).append('\n');
        for (String parent : parents) {
            builder.append("parent ").append(parent).append('\n');
        }
        builder
            .append("author ").append(author).append(' ').append(authorDate).append('\n')
            .append("committer ").append(committer).append(' ').append(committerDate).append('\n')
            .append('\n');
        if (message != null && !message.isEmpty()) {
            builder.append(message);
            if (message.charAt(message.length() - 1) != '\n') {
                builder.append('\n');
            }
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }
}
