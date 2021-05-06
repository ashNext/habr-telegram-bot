package habr.telegram.bot.habrtelegrambot;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class DateTimeUtils {

    static final String PATTERN_DATE = "d M yyyy";
    static final String PATTERN_DATE_TIME = PATTERN_DATE + " HH:mm";
    static final DateTimeFormatter dateFormatter = DateTimeFormatter
            .ofPattern(PATTERN_DATE).withZone(ZoneId.systemDefault());
    static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter
            .ofPattern(PATTERN_DATE_TIME).withZone(ZoneId.systemDefault());
    static final DateTimeFormatter dateTimePublishedFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm'Z'").withZone(ZoneId.from(ZoneOffset.UTC));

    static final Map<String, Integer> MONTHS_MAP = Map.ofEntries(
            Map.entry("января", 1),
            Map.entry("февраля", 2),
            Map.entry("марта", 3),
            Map.entry("апреля", 4),
            Map.entry("мая", 5),
            Map.entry("июня", 6),
            Map.entry("июля", 7),
            Map.entry("августа", 8),
            Map.entry("сентября", 9),
            Map.entry("октября", 10),
            Map.entry("ноября", 11),
            Map.entry("декабря", 12));

    static final String TODAY = "СЕГОДНЯ";
    static final String YESTERDAY = "ВЧЕРА";

    public static Instant parseDataTimePublished(String value) {
        return dateTimePublishedFormatter.parse(value, Instant::from);
    }

    public static Instant parseDateTime(String value) {

        String dateTimeS = value.trim().toUpperCase();
        dateTimeS = dateTimeS.replace(" В ", " ");

        if (dateTimeS.contains(TODAY)) {
            dateTimeS = dateTimeS.replace(TODAY, dateFormatter.format(Instant.now()));
        } else if (dateTimeS.contains(YESTERDAY)) {
            dateTimeS = dateTimeS.replace(YESTERDAY,
                    dateFormatter.format(Instant.now().minus(1, ChronoUnit.DAYS)));
        } else {
            String month = dateTimeS.split(" ")[1];
            dateTimeS = dateTimeS.replace(month, MONTHS_MAP.get(month.toLowerCase()).toString());
        }
        return dateTimeFormatter.parse(dateTimeS, Instant::from);
    }
}
