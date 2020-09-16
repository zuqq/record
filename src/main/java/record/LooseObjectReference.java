package record;

public class LooseObjectReference<T extends LooseObject> {
    private final byte[] targetHash;

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
