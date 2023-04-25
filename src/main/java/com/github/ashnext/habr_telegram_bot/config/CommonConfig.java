package com.github.ashnext.habr_telegram_bot.config;

import com.github.ashnext.habr_telegram_bot.config.properties.BotProperties;
import com.github.ashnext.habr_telegram_bot.telegram.api.TgmBot;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class CommonConfig {

    private final BotProperties botProperties;

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    @Bean
    public TgmBot TgmBotService(OkHttpClient okHttpClient) {
        return new TgmBot(botProperties.getToken(), okHttpClient, botProperties.getReportingTelegramUserId());
    }

}
