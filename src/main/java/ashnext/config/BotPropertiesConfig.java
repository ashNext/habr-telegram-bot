package ashnext.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "bot")
public class BotPropertiesConfig {

    private String name = "";
    private String token;
    private String lastPostUrl = "";
    private Scheduled scheduled;

    @Getter
    @Setter
    public static class Scheduled {
        private String update = "1000";
        private String newPosts = "60000";
    }
}
