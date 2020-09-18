package record;

/**
 * A reference to a loose object.
 *
 * A loose object is uniquely determined by it's hash, so that's what the
 * reference contains. The type parameter indicates what type of object the
 * reference points to.
 */
public class LooseObjectReference<T extends LooseObject> {
    private final byte[] targetHash;

    // TODO: Replace this by a constructor that takes a `T`!
    public LooseObjectReference(byte[] targetHash) {
        if (targetHash.length != 20) {
            throw new IllegalArgumentException("Expected 20 bytes.");
        }
        this.targetHash = targetHash;
    }

    public byte[] getTargetHash() {
        return targetHash;
    }

    /**
     * Returns the target's Base16-encoded hash.
     */
    @Override
    public String toString() {
        return Base16.encode(targetHash);
    }
}
