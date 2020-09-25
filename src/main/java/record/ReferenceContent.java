package record;

/**
 * The content of a git reference.
 *
 * A reference points to a commit or another reference; in the latter case we
 * say that the reference is symbolic. The HEAD file is usually a symbolic
 * reference to a branch, but it can also refer to a commit ("detached HEAD").
 */
public final class ReferenceContent {
    private final boolean symbolic;
    private final String target;

    public ReferenceContent(String content) {
        symbolic = content.startsWith("ref: ");
        target = symbolic ? content.substring(5) : content;
    }

    public ReferenceContent(Commit commit) {
        symbolic = false;
        target = Base16.encode(commit.getHash());
    }

    public ReferenceContent(boolean symbolic, String target) {
        this.symbolic = symbolic;
        this.target = target;
    }

    public boolean isSymbolic() {
        return symbolic;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return symbolic ? "ref: " + target : target;
    }
}
