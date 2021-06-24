package ashnext.telegram.tasks;

import ashnext.model.User;
import ashnext.service.UserService;
import ashnext.telegram.api.TgmBot;
import ashnext.telegram.api.response.ResponseUpdates;
import ashnext.telegram.api.types.*;
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

    public void changeUserStatus(ChatMemberUpdated chatMemberUpdated) {
        TgmUser tgmUser = chatMemberUpdated.getUser();
        User user = userService.getByTelegramUserId(tgmUser.getId());

        if (user == null) {
            log.warn("Changing the user's status is not possible because user with id={} is not registered",
                    tgmUser.getId());
        } else {
            ChatMember oldChatMember = chatMemberUpdated.getOldChatMember();
            ChatMember newChatMember = chatMemberUpdated.getNewChatMember();

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
    }

    public void processMessage(Message message) {
        String firstName = message.getTgmUser().getFirstName();
        String msg;
        User user = userService.getByTelegramUserId(message.getTgmUser().getId());
        if (message.getText().equalsIgnoreCase("/start")) {
            if (user != null) {
                msg = String.format("Welcome back, %s (id=%d) !", firstName, user.getTelegramUserId());
            } else {
                user = userService.create(new User(message.getTgmUser().getId()));
                msg = String.format("Hi, %s (id=%d) !", firstName, user.getTelegramUserId());
            }
        } else {
            if (user == null) {
                msg = "You are not registered. Go to /start";
                log.warn("User with id={} is not registered", message.getTgmUser().getId());
            } else {
                msg = "Help me, " + firstName;
            }
        }

        tgmBot.sendMessage(message.getChat().getId(), msg);
    }

    public void defineUpdate(Update update) {
        if (update.getMyChatMember() != null) {
            changeUserStatus(update.getMyChatMember());
        } else if (update.getMessage() != null) {
            processMessage(update.getMessage());
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void update() {
        Optional<ResponseUpdates> optResponseUpdates = tgmBot.getUpdates(updateId + 1, 100);

        optResponseUpdates.ifPresent(responseUpdates -> {
            for (Update update : responseUpdates.getResult()) {
                updateId = update.getUpdateId();

                defineUpdate(update);
            }
        });
    }
}
