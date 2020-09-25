package record;

/**
 * An unchecked exception for fatal (i.e., unsalvageable) parse errors.
 */
public class FatalParseException extends RuntimeException {
    private static final long serialVersionUID = -565495143174105037L;

    public FatalParseException(String message) {
        super(message);
    }
}
