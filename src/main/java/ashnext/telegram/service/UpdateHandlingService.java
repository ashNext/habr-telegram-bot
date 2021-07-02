package ashnext.telegram.service;

import ashnext.model.User;
import ashnext.service.UserService;
import ashnext.telegram.api.types.ChatMember;
import ashnext.telegram.api.types.ChatMemberUpdated;
import ashnext.telegram.api.types.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateHandlingService {

    private final UserService userService;

    public String processMessage(Message message) {
        final String firstName = message.getTgmUser().getFirstName();
        final Long tgmUserId = message.getTgmUser().getId();

        String msg;
        User user = userService.getByTelegramUserId(tgmUserId);
        if (message.getText().equalsIgnoreCase("/start")) {
            if (user != null) {
                msg = String.format("Welcome back, %s!", firstName);
                log.info("Registered user (tgmUserId={}) went to /start", tgmUserId);
            } else {
                user = userService.create(new User(tgmUserId, message.getChat().getId()));
                msg = String.format("Hi, %s!", firstName);
                log.info("Added new user ({})", user);
            }
        } else {
            if (user == null) {
                msg = "You are not registered. Go to /start";
                log.info("User with tgmUserId={} is not registered", tgmUserId);
            } else if (message.getText().equalsIgnoreCase("/sub")) {
                userService.subscribe(user);
                msg = "You subscribed";
                log.info("User ({}) subscribed", user);
            } else if (message.getText().equalsIgnoreCase("/unsub")) {
                userService.unsubscribe(user);
                msg = "You unsubscribed";
                log.info("User ({}) unsubscribed", user);
            } else {
                msg = "I don't understand yet ((";
                log.warn("Unprocessed user (tgmUserId={}) message '{}'", tgmUserId, message.getText());
            }
        }

        return msg;
    }

    public void changeUserStatus(ChatMemberUpdated chatMemberUpdated) {
        final Long tgmUserId = chatMemberUpdated.getUser().getId();
        final User user = userService.getByTelegramUserId(tgmUserId);

        if (user == null) {
            log.warn("Changing the user's status is not possible because user with id={} is not registered", tgmUserId);
        } else {
            ChatMember oldChatMember = chatMemberUpdated.getOldChatMember();
            ChatMember newChatMember = chatMemberUpdated.getNewChatMember();

            if (oldChatMember.getStatus().equalsIgnoreCase("member")
                    && newChatMember.getStatus().equalsIgnoreCase("kicked")
                    && user.isActive()) {
                userService.setActive(user, false);
                log.info("User (tgmUserId={}) disabled", tgmUserId);
            } else if (oldChatMember.getStatus().equalsIgnoreCase("kicked")
                    && newChatMember.getStatus().equalsIgnoreCase("member")
                    && !user.isActive()) {
                userService.setActive(user, true);
                log.info("User (tgmUserId={}) enabled", tgmUserId);
            }
        }
    }
}
