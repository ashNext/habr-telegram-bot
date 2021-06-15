package habr.telegram.bot.habrtelegrambot.parse.model;

import java.time.Instant;
import java.util.List;
import lombok.Value;

@Value
public class Post {

    String url;
    String header;
    Instant dateTime;
    List<String> tags;
    String content;
}