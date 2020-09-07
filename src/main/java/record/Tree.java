package record;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;

public class Tree implements Record {
    private final Node[] children;

    public Tree(Node[] children) {
        Arrays.sort(children, Comparator.comparing(Node::getName));
        this.children = children;
    }

    public static Tree parse(byte[] input) {
        // TODO
        throw new RuntimeException("Not implemented!");
    }

    public byte[] getBytes() {
        byte[][] entries = new byte[children.length][];
        int length = 0;
        for (int i = 0; i < children.length; ++i) {
            entries[i] = children[i].toEntry();
            length += entries[i].length;
        }
        byte[] header = String
            .format("tree %d\0", length)
            .getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[header.length + length];
        System.arraycopy(header, 0, result, 0, header.length);
        int offset = header.length;
        for (int i = 0; i < entries.length; ++i) {
            System.arraycopy(entries[i], 0, result, offset, entries[i].length);
            offset += entries[i].length;
        }
        return result;
    }
}
