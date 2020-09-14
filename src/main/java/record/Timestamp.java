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

    public static ZonedDateTime of(String input) {
        return ZonedDateTime.parse(input, formatter);
    }

    @Override
    public String toString() {
        return timestamp.format(formatter);
    }
}
