package ashnext.telegram.service;

import ashnext.model.User;
import ashnext.service.UserService;
import ashnext.telegram.api.types.ChatMember;
import ashnext.telegram.api.types.ChatMemberUpdated;
import ashnext.telegram.api.types.Message;
import ashnext.telegram.api.types.TgmUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateHandlingService {

    private final UserService userService;

    public String processMessage(Message message) {
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

        return msg;
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
}
