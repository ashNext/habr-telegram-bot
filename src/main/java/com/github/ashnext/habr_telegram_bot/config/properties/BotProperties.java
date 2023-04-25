package com.github.ashnext.habr_telegram_bot.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "bot")
public class BotProperties {

    private String name = "";
    private String token;
    private Scheduled scheduled;
    private Long reportingTelegramUserId;

    @Getter
    @Setter
    public static class Scheduled {
        private String update = "1000";
        private String newPosts = "300000";
    }
}
