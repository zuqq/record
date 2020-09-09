package record;

import java.nio.charset.StandardCharsets;

public class Commit implements Record {
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
    
    public byte[] getBytes() {
        StringBuilder builder = new StringBuilder(
            String.format("tree %s\n", Base16.encode(tree.getHash()))
        );
        for (Commit parent : parents) {
            builder.append(String.format("parent %s\n", parent.getHash()));
        }
        builder
            .append(String.format("author %s %s\n", author, authorDate))
            .append(String.format("committer %s %s\n", committer, committerDate))
            .append('\n')
            .append(message).append('\n');
        byte[] data = builder.toString().getBytes(StandardCharsets.UTF_8);
        byte[] header = String
            .format("commit %d\0", data.length)
            .getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[header.length + data.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(data, 0, result, header.length, data.length);
        return result;
    }
}
