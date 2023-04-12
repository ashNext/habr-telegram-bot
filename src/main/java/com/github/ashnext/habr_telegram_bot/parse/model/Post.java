package com.github.ashnext.habr_telegram_bot.parse.model;

import java.time.Instant;
import java.util.List;
import lombok.Value;

@Value
public class Post {

    String url;
    String header;
    Instant dateTime;
    List<String> hubs;
    List<String> tags;
    String content;
}
