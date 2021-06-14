package habr.telegram.bot.habrtelegrambot.telegram.api.config;

import habr.telegram.bot.habrtelegrambot.telegram.api.TgmBot;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:telegram.properties")
public class TgmBotConfig {

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    private final OkHttpClient okHttpClient;

    public TgmBotConfig(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    @Bean
    public TgmBot tgmBot() {
        return new TgmBot(botToken, okHttpClient);
    }
}
