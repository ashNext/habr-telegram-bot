package ashnext.telegram.tasks;

import ashnext.model.User;
import ashnext.service.UserService;
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

    private final UserService userService;

    public UpdateTask(TgmBotService tgmBotService, UserService userService) {
        this.tgmBot = tgmBotService.getTgmBot();
        this.userService = userService;
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
                    User user = userService.create(new User( message.getTgmUser().getId()));
                    msg = String.format("Hi, %s (id=%d) !", firstName, user.getTelegramUserId());
                }
                msg = msg + " - resp on updateId=" + updateId + " and text=" + message.getText();

                tgmBot.sendMessage(chatId, msg);
            }
        });
    }
}
