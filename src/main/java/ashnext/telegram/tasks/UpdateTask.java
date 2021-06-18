package ashnext.telegram.tasks;

import ashnext.telegram.service.TgmBotService;
import ashnext.telegram.api.TgmBot;
import ashnext.telegram.api.response.ResponseUpdates;
import ashnext.telegram.api.types.Message;
import ashnext.telegram.api.types.Update;
import java.util.Optional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UpdateTask {

    private int updateId = 0;

    private final TgmBot tgmBot;

    public UpdateTask(TgmBotService tgmBotService) {
        this.tgmBot = tgmBotService.getTgmBot();
    }

    @Scheduled(fixedDelay = 1000)
    public void update() {
        Optional<ResponseUpdates> optResponseUpdates = tgmBot.getUpdates(updateId + 1, 100);

        optResponseUpdates.ifPresent(responseUpdates -> {
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
        });
    }
}
