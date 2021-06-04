package habr.telegram.bot.habrtelegrambot.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

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
        TelegramBot bot = new TelegramBot(botToken);

        GetUpdates getUpdates = new GetUpdates().limit(100).offset(updateId + 1).timeout(0);

        GetUpdatesResponse updatesResponse = bot.execute(getUpdates);
        List<Update> updates = updatesResponse.updates();

        for (Update update : updates) {
            Message message = update.message();
            updateId = update.updateId();
            long chatId = message.chat().id();

            String msg = "Help me, " + message.chat().firstName();
            if (message.text().equalsIgnoreCase("/start")) {
                msg = "Hi, " + message.chat().firstName() + "!";
            }

            bot.execute(new SendMessage(chatId, msg));
        }
    }


}
