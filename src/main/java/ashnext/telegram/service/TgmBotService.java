package ashnext.telegram.service;

import ashnext.config.YamlPropertySourceFactory;
import ashnext.telegram.api.TgmBot;
import lombok.Getter;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

@Service
@PropertySource(value = "classpath:telegram-"+"${spring.profiles.active:prod}"+".yaml", factory = YamlPropertySourceFactory.class)
@Getter
public class TgmBotService {

    private final TgmBot tgmBot;

    public TgmBotService(OkHttpClient okHttpClient, @Value("${bot.token}") String botToken) {
        tgmBot = new TgmBot(botToken, okHttpClient);
    }
}
