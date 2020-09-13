package record;

import java.nio.charset.StandardCharsets;

public final class Commit implements LooseObject {
    private final Tree tree;
    private final Commit[] parents;
    private final User author;
    private final Timestamp authorDate;
    private final User committer;
    private final Timestamp committerDate;
    private final String message;

    public Commit(
        Tree tree,
        Commit[] parents,
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

    @Override
    public String getTag() {
        return "commit";
    }
    
    @Override
    public byte[] getBody() {
        // Unlike trees, commits use Base16-encoded hashes of the objects they refer to.
        StringBuilder builder = new StringBuilder(
            String.format("tree %s\n", Base16.encode(tree.getHash()))
        );
        for (Commit parent : parents) {
            builder.append(String.format("parent %s\n", Base16.encode(parent.getHash())));
        }
        builder
            .append(String.format("author %s %s\n", author, authorDate))
            .append(String.format("committer %s %s\n", committer, committerDate))
            .append('\n')
            .append(message).append('\n');
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }
}
