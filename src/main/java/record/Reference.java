package record;

/**
 * A reference points to a commit or another reference; in the latter case we
 * say that the reference is symbolic. The HEAD file is usually a symbolic
 * reference to a branch, but it can also refer to a commit ("detached HEAD").
 */
public final class Reference {
    private final String name;
    private final boolean symbolic;
    private final String target;

    public Reference(String name, Commit commit) {
        this.name = name;
        this.symbolic = false;
        this.target = Base16.encode(commit.getHash());
    }

    public Reference(String name, boolean symbolic, String target) {
        this.name = name;
        this.symbolic = symbolic;
        this.target = target;
    }

    public static Reference of(String name, String content) {
        // Remove trailing '\n'.
        content = content.stripTrailing();
        boolean symbolic = content.startsWith("ref: ");
        return new Reference(name, symbolic, symbolic ? content.substring(5) : content);
    }

    public String getName() {
        return name;
    }

    public boolean isSymbolic() {
        return symbolic;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return symbolic ? "ref: " + target + "\n" : target + "\n";
    }
}
