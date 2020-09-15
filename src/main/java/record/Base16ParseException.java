package record;

public class Base16ParseException extends RuntimeException {
    private static final long serialVersionUID = -565495143174105037L;

    public Base16ParseException(String message) {
        super(message);
    }
}
