package record;

public class FatalParseException extends RuntimeException {
    private static final long serialVersionUID = -565495143174105037L;

    public FatalParseException(String message) {
        super(message);
    }
}
