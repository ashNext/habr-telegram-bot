package habr.telegram.bot.habrtelegrambot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

  public static LocalDateTime parseDateTime(String value) {
    final String patternDate = "d M yyyy";
    final String patternDateTime = patternDate + " HH:mm";
    final String today = "сегодня".toUpperCase();
    final String yesterday = "вчера".toUpperCase();
    final String[] months = {
        "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря"};

    String dateTimeS = value.trim().toUpperCase();

    dateTimeS = dateTimeS.replace(" В ", " ");

    if (dateTimeS.contains(today)) {
      dateTimeS = dateTimeS.replace(today, DateTimeFormatter.ofPattern(patternDate).format(LocalDate.now()));
    } else if (dateTimeS.contains(yesterday)) {
      dateTimeS = dateTimeS.replace(yesterday, DateTimeFormatter.ofPattern(patternDate).format(LocalDate.now().minusDays(1)));
    } else {
      for (int i = 0; i < months.length; i++) {
        if (dateTimeS.contains(months[i].toUpperCase())) {
          dateTimeS = dateTimeS.replace(months[i].toUpperCase(), i + 1 + "");
          break;
        }
      }
    }

    return LocalDateTime.parse(dateTimeS, DateTimeFormatter.ofPattern(patternDateTime));
  }
}
