package ashnext.telegram.tasks;

import ashnext.model.User;
import ashnext.service.UserService;
import ashnext.telegram.api.TgmBot;
import ashnext.telegram.api.response.ResponseUpdates;
import ashnext.telegram.api.types.ChatMember;
import ashnext.telegram.api.types.Message;
import ashnext.telegram.api.types.TgmUser;
import ashnext.telegram.api.types.Update;
import ashnext.telegram.service.TgmBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UpdateTask {

    private int updateId = 0;

    private final TgmBot tgmBot;

    private final UserService userService;

    private final Logger log = LoggerFactory.getLogger(getClass());

    public UpdateTask(TgmBotService tgmBotService, UserService userService) {
        this.tgmBot = tgmBotService.getTgmBot();
        this.userService = userService;
    }

    @Scheduled(fixedDelay = 1000)
    public void update() {
        Optional<ResponseUpdates> optResponseUpdates = tgmBot.getUpdates(updateId + 1, 100);

        optResponseUpdates.ifPresent(responseUpdates -> {
            for (Update update : responseUpdates.getResult()) {
                updateId = update.getUpdateId();

                if (update.getMyChatMember() != null) {
                    TgmUser tgmUser = update.getMyChatMember().getUser();
                    User user = userService.getByTelegramUserId(tgmUser.getId());
                    if (user == null) {
                        log.error("Changing the user's status is not possible because user with id={} is not registered",
                                tgmUser.getId());
                        continue;
                    }

                    ChatMember oldChatMember = update.getMyChatMember().getOldChatMember();
                    ChatMember newChatMember = update.getMyChatMember().getNewChatMember();

                    if (oldChatMember.getStatus().equalsIgnoreCase("member")
                            && newChatMember.getStatus().equalsIgnoreCase("kicked")
                            && user.isActive()) {
                        userService.setActive(user, false);
                    } else if (oldChatMember.getStatus().equalsIgnoreCase("kicked")
                            && newChatMember.getStatus().equalsIgnoreCase("member")
                            && !user.isActive()) {
                        userService.setActive(user, true);
                    }
                }

                if (update.getMessage() != null) {
                    Message message = update.getMessage();
                    int chatId = message.getChat().getId();

                    String firstName = message.getChat().getFirstName();
                    String msg = "Help me, " + firstName;
                    if (message.getText().equalsIgnoreCase("/start")) {
                        User user = userService.getByTelegramUserId(message.getTgmUser().getId());
                        if (user != null && user.isActive()) {
                            msg = String.format("Welcome back, %s (id=%d) !", firstName, user.getTelegramUserId());
                        } else {
                            user = userService.create(new User(message.getTgmUser().getId()));
                            msg = String.format("Hi, %s (id=%d) !", firstName, user.getTelegramUserId());
                        }
                    }
                    msg = msg + " - resp on updateId=" + updateId + " and text=" + message.getText();

                    tgmBot.sendMessage(chatId, msg);
                }

            }
        });
    }
}
