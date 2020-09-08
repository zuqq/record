package record;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class Commit implements Record {
    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
        .appendValue(ChronoField.INSTANT_SECONDS)
        .appendLiteral(' ')
        .appendOffset("+HHMM", "+0000")
        .toFormatter();

    private final Tree tree;
    private final Commit[] parents;
    private final User author;
    private final ZonedDateTime authorDate;
    private final User committer;
    private final ZonedDateTime committerDate;
    private final String message;

    public Commit(
        Tree tree,
        Commit[] parents,
        User author,
        ZonedDateTime authorDate,
        User committer,
        ZonedDateTime committerDate,
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
        StringBuilder builder = new StringBuilder();
        builder
            .append(String.format("tree %s\n", Base16.encode(tree.getHash())));
        for (Commit c : parents) {
            builder.append(String.format("parent %s\n", c.getHash()));
        }
        builder
            .append(String.format("author %s %s\n", author.toString(), authorDate.format(formatter)))
            .append(String.format("committer %s %s\n", committer.toString(), committerDate.format(formatter)))
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
