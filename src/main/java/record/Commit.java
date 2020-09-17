package record;

import java.nio.charset.StandardCharsets;
import java.util.List;

public final class Commit implements LooseObject {
    private final LooseObjectReference<Tree> treeReference;
    private final List<LooseObjectReference<Commit>> parents;
    private final User author;
    private final Timestamp authorDate;
    private final User committer;
    private final Timestamp committerDate;
    private final String message;

    public Commit(LooseObjectReference<Tree> treeReference,
                  List<LooseObjectReference<Commit>> parents,
                  User author,
                  Timestamp authorDate,
                  User committer,
                  Timestamp committerDate,
                  String message) {
        this.treeReference = treeReference;
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
        StringBuilder builder = new StringBuilder(String.format("tree %s\n", treeReference));
        for (LooseObjectReference<Commit> parent : parents) {
            builder.append(String.format("parent %s\n", parent));
        }
        builder
                .append(String.format("author %s %s\n", author, authorDate))
                .append(String.format("committer %s %s\n", committer, committerDate))
                .append('\n')
                .append(message).append('\n');
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }
}
