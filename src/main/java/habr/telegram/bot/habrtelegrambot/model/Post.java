package habr.telegram.bot.habrtelegrambot.model;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@AllArgsConstructor
public class Post {
    String url;
    String header;
    Instant dateTime;
    List<String> tags;
    String content;
}
