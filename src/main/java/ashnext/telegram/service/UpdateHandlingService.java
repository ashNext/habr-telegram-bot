package ashnext.telegram.service;

import ashnext.model.ReadLater;
import ashnext.model.Tag;
import ashnext.model.TagGroup;
import ashnext.model.User;
import ashnext.parse.model.Post;
import ashnext.service.ReadLaterService;
import ashnext.service.TagService;
import ashnext.service.UserService;
import ashnext.telegram.api.types.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateHandlingService {

    private final TgmBotService tgmBotService;

    private final UserService userService;

    private final ReadLaterService readLaterService;

    private final ParseHabrService parseHabrService;

    private final TagService tagService;

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
            } else if (message.getText().equalsIgnoreCase("/rlater")) {
                tgmBotService.getTgmBot().deleteMessage(message.getChat().getId(), message.getMessageId());
                buttons = getReadLaterButtons(user);

                msg = "List Read later:";
                if (buttons.getInlineKeyboard().length == 0) {
                    msg = msg + " empty";
                }
            } else if (message.getText().equalsIgnoreCase("/tags")) {
                tgmBotService.getTgmBot().deleteMessage(message.getChat().getId(), message.getMessageId());
                buttons = getTagManagementButtons();

                msg = "Tag management:";
                if (buttons.getInlineKeyboard().length == 0) {
                    msg = msg + " empty";
                }
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
        final int chatId = cbqMessage.getChat().getId();
        final int messageId = cbqMessage.getMessageId();

        if (cbqData.equalsIgnoreCase("delete") || cbqData.equalsIgnoreCase("close")) {
            tgmBotService.getTgmBot().deleteMessage(chatId, messageId);
            tgmBotService.getTgmBot().answerCallbackQuery(callbackQuery.getId(),
                    cbqData.equalsIgnoreCase("delete") ? "Deleted" : "");
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
                    tgmBotService.getTgmBot().deleteMessage(chatId, messageId);
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
                        chatId,
                        optReadLater.get().getPostUrl(),
                        getButtonsForPostFromReadLater());
                tgmBotService.getTgmBot().answerCallbackQuery(callbackQuery.getId(), "");
            } else {
                tgmBotService.getTgmBot().answerCallbackQuery(
                        callbackQuery.getId(),
                        "Post would be removed from the list Read later");
            }

        } else if (cbqData.startsWith("tg:")) {
            String answerCallbackQueryText = "";
            String captionMenu;
            InlineKeyboardMarkup keyboard;
            List<String> dataList = Arrays.stream(cbqData.split(":")).toList();

            if (dataList.get(1).equals("men")) {
                captionMenu = "Tag management";
                keyboard = getTagManagementButtons();
            } else {
                Page<Tag> pageTags = null;
                int page = Integer.parseInt(dataList.get(4));
                String tagsButtonsData = "";
                String controlButtonsData = "";

                switch (dataList.get(3)) {
                    case "a" -> {
                        Optional<Tag> addedTag = userService
                                .addTagByUserIdAndTagId(user.getId(), UUID.fromString(dataList.get(5)));

                        answerCallbackQueryText = addedTag
                                .map(tag -> "Tag " + tag.getName() + " added")
                                .orElse("The tag has already been added");
                        if (cbqData.startsWith("tg:all:")) {
                            tgmBotService.getTgmBot().
                                    answerCallbackQuery(callbackQuery.getId(), answerCallbackQueryText);
                            return;
                        }
                    }
                    case "r" -> {
                        Optional<Tag> removedTag = userService
                                .removeTagByUserIdAndTagId(user.getId(), UUID.fromString(dataList.get(5)));

                        answerCallbackQueryText = removedTag
                                .map(tag -> "Tag " + tag.getName() + " removed")
                                .orElse("The tag has already been removed");
                    }
                }

                TagGroup tagGroup;
                if ("b".equals(dataList.get(2))) {
                    captionMenu = "company blogs";
                    tagGroup = TagGroup.BLOG;
                } else {
                    captionMenu = "common";
                    tagGroup = TagGroup.COMMON;
                }

                switch (dataList.get(1)) {
                    case "all" -> {
                        captionMenu = "All " + captionMenu + "\n(click to add)";
                        pageTags = tagService.getAllByTagGroup(tagGroup, page, 20);
                        controlButtonsData = "tg:all:" + dataList.get(2);
                        tagsButtonsData = controlButtonsData + ":a";
                    }
                    case "wom" -> {
                        captionMenu = "Without my " + captionMenu + "\n(click to add)";
                        pageTags = tagService.getWithoutUserTags(user.getId(), tagGroup, page, 20);
                        controlButtonsData = "tg:wom:" + dataList.get(2);
                        tagsButtonsData = controlButtonsData + ":a";
                    }
                    case "my" -> {
                        captionMenu = "My " + captionMenu + "\n(click to remove)";
                        pageTags = userService.getByIdAndTagGroup(user.getId(), tagGroup, page, 20);
                        controlButtonsData = "tg:my:" + dataList.get(2);
                        tagsButtonsData = controlButtonsData + ":r";
                    }
                }

                if (pageTags != null && pageTags.hasContent()) {
                    page = pageTags.getNumber();
                    captionMenu = captionMenu + " [page " + (page + 1) + " of " + pageTags.getTotalPages() + "]";
                } else {
                    captionMenu = captionMenu + " [empty]";
                }

                keyboard = getTagsPageableButtons(pageTags, tagsButtonsData, controlButtonsData + ":s");
            }

            tgmBotService.getTgmBot().editMessageText(chatId, messageId, captionMenu, keyboard);
            tgmBotService.getTgmBot().answerCallbackQuery(callbackQuery.getId(), answerCallbackQueryText);
        }

    }

    private InlineKeyboardMarkup getReadLaterButtons(User user) {
        List<ReadLater> readLaterList = readLaterService.getAllByUser(user);
        InlineKeyboardButton[][] buttons = new InlineKeyboardButton[readLaterList.size() + 1][1];

        for (int i = 0; i < readLaterList.size(); i++) {
            InlineKeyboardButton button =
                    new InlineKeyboardButton(
                            readLaterList.get(i).getPostTitle(),
                            "rl:" + readLaterList.get(i).getId(),
                            "");
            buttons[i][0] = button;
        }

        buttons[readLaterList.size()][0] = new InlineKeyboardButton("\uD83C\uDD91 Close", "close", "");

        return new InlineKeyboardMarkup(buttons);
    }

    private InlineKeyboardMarkup getButtonsForPostFromReadLater() {
        InlineKeyboardButton[][] buttons = new InlineKeyboardButton[][]{{
                new InlineKeyboardButton("\uD83D\uDCE4", "remove-read-later", ""),
                new InlineKeyboardButton("\uD83D\uDDD1", "delete", "")
        }};

        return new InlineKeyboardMarkup(buttons);
    }

    private InlineKeyboardMarkup getTagsPageableButtons(Page<Tag> pageTags,
                                                        String answerPrefixCallbackQuery,
                                                        String buttonPrefixCallbackQuery) {
        List<Tag> tags;
        int page = 0;
        if (pageTags != null && pageTags.hasContent()) {
            tags = pageTags.getContent();
            page = pageTags.getNumber();
        } else {
            tags = List.of();
        }

        int kbCountLines = tags.size() % 2 == 0 ? tags.size() / 2 : tags.size() / 2 + 1;
        InlineKeyboardButton[][] buttons = new InlineKeyboardButton[kbCountLines + 1][];

        Iterator<Tag> iterator = tags.iterator();
        for (int i = 0; i < kbCountLines; i++) {
            if (i == kbCountLines - 1 && tags.size() % 2 != 0) {
                buttons[i] = new InlineKeyboardButton[1];
            } else {
                buttons[i] = new InlineKeyboardButton[2];
            }

            for (int j = 0; j < 2 && iterator.hasNext(); j++) {
                Tag tag = iterator.next();
                String buttonCaption = tag.getName();
                if (buttonCaption.startsWith("Блог компании")) {
                    buttonCaption = buttonCaption.substring(14);
                }
                InlineKeyboardButton button =
                        new InlineKeyboardButton(buttonCaption,
                                answerPrefixCallbackQuery + ":" + page + ":" + tag.getId(), "");
                buttons[i][j] = button;
            }
        }

        String callbackData = buttonPrefixCallbackQuery + ":";

        if (pageTags != null && pageTags.getTotalPages() != 0) {
            buttons[kbCountLines] = new InlineKeyboardButton[6];
            buttons[kbCountLines][4] = new InlineKeyboardButton(
                    pageTags.isLast() ? "" : "\u25B6", callbackData + (page + 1), "");
            buttons[kbCountLines][5] = new InlineKeyboardButton(
                    pageTags.isLast() ? "" : "\u23E9",
                    callbackData + (pageTags.getTotalPages() - 1), "");
        } else {
            buttons[kbCountLines] = new InlineKeyboardButton[4];
        }

        buttons[kbCountLines][0] = new InlineKeyboardButton(
                page == 0 ? "" : "\u23EA", callbackData + 0, "");
        buttons[kbCountLines][1] = new InlineKeyboardButton(
                page == 0 ? "" : "\u25C0", callbackData + (page - 1), "");
        buttons[kbCountLines][2] = new InlineKeyboardButton("\u21A9", "tg:men", "");
        buttons[kbCountLines][3] = new InlineKeyboardButton("\uD83C\uDD91", "close", "");

        return new InlineKeyboardMarkup(buttons);
    }

    private InlineKeyboardMarkup getTagManagementButtons() {
        InlineKeyboardButton[][] buttons = new InlineKeyboardButton[][]{{
                new InlineKeyboardButton("All common", "tg:all:c:s:0", ""),
                new InlineKeyboardButton("All company blogs", "tg:all:b:s:0", ""),
        }, {
                new InlineKeyboardButton("Without my common", "tg:wom:c:s:0", ""),
                new InlineKeyboardButton("Without my company blogs", "tg:wom:b:s:0", ""),
        }, {
                new InlineKeyboardButton("My common", "tg:my:c:s:0", ""),
                new InlineKeyboardButton("My company blogs", "tg:my:b:s:0", "")
        }, {
                new InlineKeyboardButton("\uD83C\uDD91 Close", "close", "")
        }};

        return new InlineKeyboardMarkup(buttons);
    }
}
