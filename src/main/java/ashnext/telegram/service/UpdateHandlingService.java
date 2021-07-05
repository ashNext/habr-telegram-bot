package ashnext.telegram.service;

import ashnext.model.ReadLater;
import ashnext.model.User;
import ashnext.parse.model.Post;
import ashnext.service.ReadLaterService;
import ashnext.service.UserService;
import ashnext.telegram.api.types.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateHandlingService {

    private final UserService userService;

    private final ReadLaterService readLaterService;

    private final ParseHabrService parseHabrService;

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

    public String kb(CallbackQuery callbackQuery) {
        final Long tgmUserId = callbackQuery.getUser().getId();
        final User user = userService.getByTelegramUserId(tgmUserId);

        if (callbackQuery.getData().equalsIgnoreCase("read-later")) {
            String postUrl = callbackQuery.getMessage().getText();

            try {
                Optional<Post> optPost = parseHabrService.parseAndGetPost(postUrl);
                if (optPost.isPresent()) {
                    readLaterService.create(new ReadLater(user, postUrl, optPost.get().getHeader()));
                    return "Added for reading later:\n" + optPost.get().getHeader();
                }
            } catch (IOException e) {
                log.error("Error in kb", e);
            }
        }

        return null;
    }

    public InlineKeyboardMarkup getReadLaterButtons(Message message) {
        final Long tgmUserId = message.getTgmUser().getId();
        User user = userService.getByTelegramUserId(tgmUserId);

        List<ReadLater> readLaterList = readLaterService.getAllByUser(user);
        InlineKeyboardButton[][] buttons = new InlineKeyboardButton[readLaterList.size()][1];

        for (int i = 0; i < readLaterList.size(); i++) {
            InlineKeyboardButton button =
                    new InlineKeyboardButton(
                            readLaterList.get(i).getPostTitle(),
                            readLaterList.get(i).getPostUrl(),
                            "");
            buttons[i][0] = button;
        }

        return new InlineKeyboardMarkup(buttons);
    }
}
