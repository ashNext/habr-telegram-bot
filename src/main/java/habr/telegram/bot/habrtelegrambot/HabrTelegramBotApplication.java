package habr.telegram.bot.habrtelegrambot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HabrTelegramBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(HabrTelegramBotApplication.class, args);
    }

}
