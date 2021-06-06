package habr.telegram.bot.habrtelegrambot.bot;

import habr.telegram.bot.habrtelegrambot.tgmApi.TgmBot;
import habr.telegram.bot.habrtelegrambot.tgmApi.response.ResponseUpdates;
import habr.telegram.bot.habrtelegrambot.tgmApi.types.Message;
import habr.telegram.bot.habrtelegrambot.tgmApi.types.Update;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:telegram.properties")
public class Bot {

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    private int updateId = 0;

    @Scheduled(fixedDelay = 1000)
    public void test() {

        TgmBot tgmBot = new TgmBot(botToken);

        ResponseUpdates responseUpdates = tgmBot.getUpdates(updateId + 1, 100);

        for (Update update : responseUpdates.getResult()) {
            Message message = update.getMessage();
            int chatId = message.getChat().getId();
            updateId = update.getUpdateId();

            String firstName = message.getChat().getFirstName();
            String msg = "Help me, " + firstName;
            if (message.getText().equalsIgnoreCase("/start")) {
                msg = "Hi, " + firstName + "!";
            }
            msg = msg + " - resp on updateId=" + updateId + " and text=" + message.getText();

            tgmBot.sendMessage(chatId, msg);
        }
    }
}
