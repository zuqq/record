package record;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public final class Timestamp {
    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.INSTANT_SECONDS)
            .appendLiteral(' ')
            .appendOffset("+HHMM", "+0000")
            .toFormatter();

    private final ZonedDateTime timestamp;

    public Timestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public static Timestamp now() {
        return new Timestamp(ZonedDateTime.now());
    }

    public static Timestamp of(String input) {
        return new Timestamp(ZonedDateTime.parse(input, formatter));
    }

    @Override
    public String toString() {
        return timestamp.format(formatter);
    }
}
