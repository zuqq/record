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

    /**
     * Type-safe constructor.
     */
    public LooseObjectReference(T target) {
        this.targetHash = target.getHash();
    }

    /**
     * Type-unsafe constructor.
     */
    public LooseObjectReference(byte[] targetHash) {
        if (targetHash.length != 20) {
            throw new IllegalArgumentException("Expected 20 bytes.");
        }
        this.targetHash = targetHash;
    }

    /**
     * Type-unsafe constructor.
     */
    public LooseObjectReference(String encodedTargetHash) {
        if (encodedTargetHash.length() != 40) {
            throw new IllegalArgumentException("Expected 40 characters.");
        }
        try {
            this.targetHash = Base16.decode(encodedTargetHash);
        } catch (FatalParseException e) {
            throw new IllegalArgumentException("Invalid Base16 string.");
        }
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
