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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateHandlingService {

    private final TgmBotService tgmBotService;

    private final UserService userService;

    private final ReadLaterService readLaterService;

    private final ParseHabrService parseHabrService;

    public void processMessage(Message message) {
        final String firstName = message.getTgmUser().getFirstName();
        final Long tgmUserId = message.getTgmUser().getId();

        String msg;
        InlineKeyboardMarkup buttons = new InlineKeyboardMarkup();

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
        } else if (message.getText().equalsIgnoreCase("/rlater")) {
            buttons = getReadLaterButtons(message);

            msg = "List Read later:";
            if (buttons.getInlineKeyboard().length == 0) {
                msg = msg + " empty";
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

        if (buttons.getInlineKeyboard() != null) {
            tgmBotService.getTgmBot().sendMessage(message.getChat().getId(), msg, buttons);
        } else {
            tgmBotService.getTgmBot().sendMessage(message.getChat().getId(), msg);
        }
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

    public void handlingCallBackQuery(CallbackQuery callbackQuery) {
        final User user = userService.getByTelegramUserId(callbackQuery.getUser().getId());
        final String cbqData = callbackQuery.getData();
        final Message cbqMessage = callbackQuery.getMessage();

        if (cbqData.equalsIgnoreCase("delete")) {
            tgmBotService.getTgmBot().deleteMessage(cbqMessage.getChat().getId(), cbqMessage.getMessageId());
            tgmBotService.getTgmBot().answerCallbackQuery(callbackQuery.getId(), "Deleted");
        } else if (cbqData.equalsIgnoreCase("read-later")) {
            final String postUrl = cbqMessage.getText();
            if (!readLaterService.getAllByUserAndPostUrl(user, postUrl).isEmpty()) {
                tgmBotService.getTgmBot().answerCallbackQuery(
                        callbackQuery.getId(),
                        "The post has already been added earlier");
                return;
            }

            final Optional<Post> optPost = parseHabrService.parseAndGetPost(postUrl);
            if (optPost.isPresent()) {
                if (readLaterService.create(new ReadLater(user, postUrl, optPost.get().getHeader())) != null) {
                    tgmBotService.getTgmBot().deleteMessage(cbqMessage.getChat().getId(), cbqMessage.getMessageId());
                    tgmBotService.getTgmBot().answerCallbackQuery(
                            callbackQuery.getId(),
                            "Post has been moved to the list Read Later");
                } else {
                    tgmBotService.getTgmBot().answerCallbackQuery(callbackQuery.getId(), "Something went wrong");
                }
            } else {
                tgmBotService.getTgmBot().answerCallbackQuery(callbackQuery.getId(), "Error parsing post header ((");
            }
        } else if (cbqData.startsWith("remove-read-later")) {
            readLaterService.getAllByUserAndPostUrl(user, cbqMessage.getText()).forEach(
                    readLater -> readLaterService.delete(readLater.getId())
            );
            tgmBotService.getTgmBot().answerCallbackQuery(callbackQuery.getId(), "Remove from the list Read later");
        } else if (cbqData.startsWith("rl:")) {
            Optional<ReadLater> optReadLater =
                    readLaterService.getByUUID(UUID.fromString(cbqData.substring(3)));
            if (optReadLater.isPresent()) {
                tgmBotService.getTgmBot().sendMessage(
                        cbqMessage.getChat().getId(),
                        optReadLater.get().getPostUrl(),
                        getButtonsForPostFromReadLater());
                tgmBotService.getTgmBot().answerCallbackQuery(callbackQuery.getId(), "");
            } else {
                tgmBotService.getTgmBot().answerCallbackQuery(
                        callbackQuery.getId(),
                        "Post would be removed from the list Read later");
            }
        }
    }

    private InlineKeyboardMarkup getReadLaterButtons(Message message) {
        final Long tgmUserId = message.getTgmUser().getId();
        User user = userService.getByTelegramUserId(tgmUserId);

        List<ReadLater> readLaterList = readLaterService.getAllByUser(user);
        InlineKeyboardButton[][] buttons = new InlineKeyboardButton[readLaterList.size()][1];

        for (int i = 0; i < readLaterList.size(); i++) {
            InlineKeyboardButton button =
                    new InlineKeyboardButton(
                            readLaterList.get(i).getPostTitle(),
                            "rl:" + readLaterList.get(i).getId(),
                            "");
            buttons[i][0] = button;
        }

        return new InlineKeyboardMarkup(buttons);
    }

    private InlineKeyboardMarkup getButtonsForPostFromReadLater() {
        InlineKeyboardButton[][] buttons = new InlineKeyboardButton[][]{{
                new InlineKeyboardButton("\uD83D\uDCE4", "remove-read-later", ""),
                new InlineKeyboardButton("\uD83D\uDDD1", "delete", "")
        }};

        return new InlineKeyboardMarkup(buttons);
    }
}
