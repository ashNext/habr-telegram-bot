package ashnext.telegram.service;

import ashnext.model.ReadLater;
import ashnext.model.Tag;
import ashnext.model.TagGroup;
import ashnext.model.User;
import ashnext.parse.model.Post;
import ashnext.service.ReadLaterService;
import ashnext.service.TagService;
import ashnext.service.UserService;
import ashnext.telegram.api.Command;
import ashnext.telegram.api.TgmBot;
import ashnext.telegram.api.types.*;
import ashnext.telegram.control.ActionTagButton;
import ashnext.telegram.control.GroupTag;
import ashnext.telegram.control.TypeTag;
import ashnext.telegram.control.button.TagButton;
import ashnext.telegram.control.button.UtilTagButton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Iterator;
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

    private final TagService tagService;

    public void processMessage(Message message) {
        final String firstName = message.getTgmUser().getFirstName();
        final Long tgmUserId = message.getTgmUser().getId();

        String msg;
        InlineKeyboardMarkup buttons = new InlineKeyboardMarkup();

        User user = userService.getByTelegramUserId(tgmUserId);

        Command command = Command.valueOfCommand(message.getText());

        if (command == Command.START) {
            if (user != null) {
                msg = String.format("Welcome back, %s!", firstName);
                log.info("Registered user (tgmUserId={}) went to {}", tgmUserId, Command.START.getCommand());
            } else {
                userService.create(new User(tgmUserId, message.getChat().getId()));
                msg = String.format("Hi, %s!", firstName);
            }
        } else if (user == null) {
            msg = "You are not registered. Go to " + Command.START.getCommand();
            log.warn("User with tgmUserId={} is not registered", tgmUserId);
        } else {
            switch (command) {
                case SUB -> {
                    userService.subscribe(user);
                    msg = "You subscribed";
                }
                case UNSUB -> {
                    userService.unsubscribe(user);
                    msg = "You unsubscribed";
                }
                case READ_LATER -> {
                    tgmBotService.getTgmBot().deleteMessage(message.getChat().getId(), message.getMessageId());
                    buttons = getReadLaterButtons(user);

                    msg = "List Read later:";
                    if (buttons.getInlineKeyboard().length == 0) {
                        msg = msg + " empty";
                    }
                }
                case TAGS -> {
                    tgmBotService.getTgmBot().deleteMessage(message.getChat().getId(), message.getMessageId());
                    buttons = getTagManagementButtons();

                    msg = "Tag management:";
                    if (buttons.getInlineKeyboard().length == 0) {
                        msg = msg + " empty";
                    }
                }
                default -> {
                    msg = "I don't understand yet ((";
                    log.warn("Unprocessed user (tgmUserId={}) message '{}'", tgmUserId, message.getText());
                }
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
            } else if (oldChatMember.getStatus().equalsIgnoreCase("kicked")
                    && newChatMember.getStatus().equalsIgnoreCase("member")
                    && !user.isActive()) {
                userService.setActive(user, true);
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
            tgmBotService.getTgmBot().editMessageText(chatId, messageId, cbqMessage.getText(), TgmBot.INLINE_KEYBOARD_MARKUP_POST_NORMAL);
            tgmBotService.getTgmBot().answerCallbackQuery(callbackQuery.getId(), "Remove from the list Read later");
        } else if (cbqData.startsWith("rl:")) {
            Optional<ReadLater> optReadLater =
                    readLaterService.getByUUID(UUID.fromString(cbqData.substring(3)));
            if (optReadLater.isPresent()) {
                tgmBotService.getTgmBot().sendMessage(
                        chatId,
                        optReadLater.get().getPostUrl(),
                        TgmBot.INLINE_KEYBOARD_MARKUP_POST_READ_LATER);
                tgmBotService.getTgmBot().answerCallbackQuery(callbackQuery.getId(), "");
                tgmBotService.getTgmBot().deleteMessage(chatId, messageId);
            } else {
                tgmBotService.getTgmBot().answerCallbackQuery(
                        callbackQuery.getId(),
                        "Post would be removed from the list Read later");
            }

        } else if (cbqData.startsWith("tg:")) {
            String answerCallbackQueryText = "";
            String captionMenu;
            InlineKeyboardMarkup keyboard;

            if (cbqData.startsWith("tg:men")) {
                captionMenu = "Tag management";
                keyboard = getTagManagementButtons();
            } else {
                TagButton pressedButton = UtilTagButton.getButton(cbqData);
                Page<Tag> pageTags = null;
                int page = pressedButton.getPage();

                switch (pressedButton.getActionTagButton()) {
                    case ADD -> {
                        Optional<Tag> addedTag = userService
                                .addTagByUserIdAndTagId(user.getId(), UUID.fromString(pressedButton.getData()));

                        answerCallbackQueryText = addedTag
                                .map(tag -> "Tag " + tag.getName() + " added")
                                .orElse("The tag has already been added");
                        if (pressedButton.getGroupTag() == GroupTag.ALL_TAGS) {
                            tgmBotService.getTgmBot().
                                    answerCallbackQuery(callbackQuery.getId(), answerCallbackQueryText);
                            return;
                        }
                    }
                    case REMOVE -> {
                        Optional<Tag> removedTag = userService
                                .removeTagByUserIdAndTagId(user.getId(), UUID.fromString(pressedButton.getData()));

                        answerCallbackQueryText = removedTag
                                .map(tag -> "Tag " + tag.getName() + " removed")
                                .orElse("The tag has already been removed");
                    }
                }

                TagGroup tagGroup;
                if (pressedButton.getTypeTag() == TypeTag.BLOG) {
                    captionMenu = "company blogs";
                    tagGroup = TagGroup.BLOG;
                } else {
                    captionMenu = "common";
                    tagGroup = TagGroup.COMMON;
                }

                switch (pressedButton.getGroupTag()) {
                    case ALL_TAGS -> {
                        captionMenu = "All " + captionMenu + "\n(click to add)";
                        pageTags = tagService.getAllByTagGroup(tagGroup, page, 20);
                    }
                    case WITHOUT_MY_TAGS -> {
                        captionMenu = "Without my " + captionMenu + "\n(click to add)";
                        pageTags = tagService.getWithoutUserTags(user.getId(), tagGroup, page, 20);
                    }
                    case MY_TAGS -> {
                        captionMenu = "My " + captionMenu + "\n(click to remove)";
                        pageTags = userService.getByIdAndTagGroup(user.getId(), tagGroup, page, 20);
                    }
                }

                if (pageTags != null && pageTags.hasContent()) {
                    page = pageTags.getNumber();
                    captionMenu = captionMenu + " [page " + (page + 1) + " of " + pageTags.getTotalPages() + "]";
                } else {
                    captionMenu = captionMenu + " [empty]";
                }

                keyboard = getTagsPageableButtons(pageTags, pressedButton);
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

    private InlineKeyboardMarkup getTagsPageableButtons(Page<Tag> pageTags, TagButton pressedTagButton) {
        ActionTagButton action;
        switch (pressedTagButton.getGroupTag()) {
            case ALL_TAGS, WITHOUT_MY_TAGS -> action = ActionTagButton.ADD;
            case MY_TAGS -> action = ActionTagButton.REMOVE;
            default -> throw new IllegalStateException("Unexpected value: " + pressedTagButton.getGroupTag());
        }

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
                                TagButton.newBuilder()
                                        .setGroup(pressedTagButton.getGroupTag())
                                        .setType(pressedTagButton.getTypeTag())
                                        .setAction(action)
                                        .setPage(page)
                                        .setData(tag.getId().toString())
                                        .build().toString(),
                                ""
                        );
                buttons[i][j] = button;
            }
        }

        TagButton.Builder controlButtonBuilder = TagButton.newBuilder()
                .setGroup(pressedTagButton.getGroupTag())
                .setType(pressedTagButton.getTypeTag())
                .setActionShow();

        if (pageTags != null && pageTags.getTotalPages() != 0) {
            buttons[kbCountLines] = new InlineKeyboardButton[6];
            buttons[kbCountLines][4] = new InlineKeyboardButton(
                    pageTags.isLast() ? "" : "\u25B6", controlButtonBuilder.setPage(page + 1).build().toString(), "");
            buttons[kbCountLines][5] = new InlineKeyboardButton(
                    pageTags.isLast() ? "" : "\u23E9",
                    controlButtonBuilder.setPage(pageTags.getTotalPages() - 1).build().toString(), "");
        } else {
            buttons[kbCountLines] = new InlineKeyboardButton[4];
        }

        buttons[kbCountLines][0] = new InlineKeyboardButton(
                page == 0 ? "" : "\u23EA", controlButtonBuilder.setPage(0).build().toString(), "");
        buttons[kbCountLines][1] = new InlineKeyboardButton(
                page == 0 ? "" : "\u25C0", controlButtonBuilder.setPage(page - 1).build().toString(), "");
        buttons[kbCountLines][2] = new InlineKeyboardButton("\u21A9", "tg:men", "");
        buttons[kbCountLines][3] = new InlineKeyboardButton("\uD83C\uDD91", "close", "");

        return new InlineKeyboardMarkup(buttons);
    }

    private InlineKeyboardMarkup getTagManagementButtons() {
        InlineKeyboardButton[][] buttons = new InlineKeyboardButton[][]{{
                new InlineKeyboardButton("All common",
                        TagButton.newBuilder().setGroupAll().setTypeCommon().setActionShow().build().toString(), ""),
                new InlineKeyboardButton("All company blogs",
                        TagButton.newBuilder().setGroupAll().setTypeBlog().setActionShow().build().toString(), ""),
        }, {
                new InlineKeyboardButton("Without my common",
                        TagButton.newBuilder().setGroupWithoutMy().setTypeCommon().setActionShow().build().toString(),
                        ""),
                new InlineKeyboardButton("Without my company blogs",
                        TagButton.newBuilder().setGroupWithoutMy().setTypeBlog().setActionShow().build().toString(),
                        ""),
        }, {
                new InlineKeyboardButton("My common",
                        TagButton.newBuilder().setGroupMy().setTypeCommon().setActionShow().build().toString(), ""),
                new InlineKeyboardButton("My company blogs",
                        TagButton.newBuilder().setGroupMy().setTypeBlog().setActionShow().build().toString(), "")
        }, {
                new InlineKeyboardButton("\uD83C\uDD91 Close", "close", "")
        }};

        return new InlineKeyboardMarkup(buttons);
    }
}
