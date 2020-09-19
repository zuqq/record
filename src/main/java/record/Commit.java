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

    @Override
    public String getTag() {
        return "commit";
    }

    @Override
    public byte[] getBody() {
        // Unlike trees, commits use Base16-encoded hashes of the objects they refer to.
        StringBuilder builder = new StringBuilder();
        builder.append("tree ").append(tree).append('\n');
        for (String parent : parents) {
            builder.append("parent ").append(parent).append('\n');
        }
        builder
                .append("author ").append(author).append(' ').append(authorDate).append('\n')
                .append("committer ").append(committer).append(' ').append(committerDate).append('\n')
                .append('\n')
                .append(message).append('\n');
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }
}
