package com.github.ashnext.habr_telegram_bot.telegram.service;

import com.github.ashnext.habr_telegram_bot.bookmark.Bookmark;
import com.github.ashnext.habr_telegram_bot.bookmark.service.BookmarkService;
import com.github.ashnext.habr_telegram_bot.parse.model.Post;
import com.github.ashnext.habr_telegram_bot.parse.service.ParseHabrService;
import com.github.ashnext.habr_telegram_bot.tag.Tag;
import com.github.ashnext.habr_telegram_bot.tag.TagGroup;
import com.github.ashnext.habr_telegram_bot.tag.service.TagService;
import com.github.ashnext.habr_telegram_bot.telegram.api.Command;
import com.github.ashnext.habr_telegram_bot.telegram.api.TgmBot;
import com.github.ashnext.habr_telegram_bot.telegram.api.types.*;
import com.github.ashnext.habr_telegram_bot.telegram.control.bookmark.BookmarkButton;
import com.github.ashnext.habr_telegram_bot.telegram.control.bookmark.BookmarkMenu;
import com.github.ashnext.habr_telegram_bot.telegram.control.tag.*;
import com.github.ashnext.habr_telegram_bot.user.User;
import com.github.ashnext.habr_telegram_bot.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static com.github.ashnext.habr_telegram_bot.telegram.api.TgmBot.TG_INSTANT_VIEW_TEMPLATE;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateHandlingService {

    private final TgmBot tgmBot;

    private final UserService userService;

    private final BookmarkService bookmarkService;

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
                case BOOKMARKS -> {
                    tgmBot.deleteMessage(message.getChat().getId(), message.getMessageId());
                    buttons = BookmarkMenu.getBookmarkButtons(bookmarkService.getAllByUser(user));

                    msg = "Bookmarks:";
                    if (buttons.getInlineKeyboard().length == 0) {
                        msg = msg + " empty";
                    }
                }
                case TAGS -> {
                    tgmBot.deleteMessage(message.getChat().getId(), message.getMessageId());
                    buttons = TagMenu.getTagManagementButtons();

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
            tgmBot.sendMessage(message.getChat().getId(), msg, buttons);
        } else {
            tgmBot.sendMessage(message.getChat().getId(), msg);
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
            tgmBot.deleteMessage(chatId, messageId);
            tgmBot.answerCallbackQuery(callbackQuery.getId(),
                    cbqData.equalsIgnoreCase("delete") ? "Deleted" : "");
        } else if (cbqData.startsWith("bm:")) {
            BookmarkButton bookmarkButton = BookmarkMenu.getButton(cbqData);

            switch (bookmarkButton.getActionBookmarkButton()) {
                case GET -> {
                    Optional<Bookmark> optBookmark =
                            bookmarkService.getByUUID(UUID.fromString(bookmarkButton.getData()));
                    if (optBookmark.isPresent()) {
                        tgmBot.sendMessage(
                                chatId,
                                String.format(TG_INSTANT_VIEW_TEMPLATE, optBookmark.get().getPostUrl()),
                                BookmarkMenu.getButtonsWithRemove());
                        tgmBot.answerCallbackQuery(callbackQuery.getId(), "");
                        tgmBot.deleteMessage(chatId, messageId);
                    } else {
                        tgmBot.answerCallbackQuery(
                                callbackQuery.getId(),
                                "Post would be removed from Bookmarks");
                    }
                }
                case PUT -> {
                    final String postUrl = getUrlFromTgUrl(cbqMessage.getText(), callbackQuery.getId());

                    if (!bookmarkService.getAllByUserAndPostUrl(user, postUrl).isEmpty()) {
                        tgmBot.answerCallbackQuery(
                                callbackQuery.getId(),
                                "The post has already been added earlier");
                        return;
                    }

                    final Optional<Post> optPost = parseHabrService.parseAndGetPost(postUrl);
                    if (optPost.isPresent()) {
                        if (bookmarkService.create(new Bookmark(user, postUrl, optPost.get().getHeader())) != null) {
                            tgmBot.deleteMessage(chatId, messageId);
                            tgmBot.answerCallbackQuery(
                                    callbackQuery.getId(),
                                    "Post has been moved to Bookmarks");
                        } else {
                            tgmBot.answerCallbackQuery(callbackQuery.getId(), "Something went wrong 😢");
                        }
                    } else {
                        tgmBot.answerCallbackQuery(callbackQuery.getId(), "Error parsing post header 😢");
                    }
                }
                case PULL -> {
                    final String postUrl = getUrlFromTgUrl(cbqMessage.getText(), callbackQuery.getId());

                    tgmBot.editMessageText(chatId, messageId, cbqMessage.getText(), BookmarkMenu.getButtonsWithAdd());
                    tgmBot.answerCallbackQuery(callbackQuery.getId(), "Remove from the Bookmarks");
                    bookmarkService.getAllByUserAndPostUrl(user, postUrl).forEach(
                            bookmark -> bookmarkService.delete(bookmark.getId())
                    );
                }
            }
        } else if (cbqData.startsWith("tg:")) {
            String answerCallbackQueryText = "";
            String captionMenu;
            InlineKeyboardMarkup keyboard;

            TagButton pressedButton = TagMenu.getButton(cbqData);

            if (pressedButton.getActionTagButton() == ActionTagButton.MANAGEMENT) {
                captionMenu = "Tag management";
                keyboard = TagMenu.getTagManagementButtons();
            } else {
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
                            tgmBot.answerCallbackQuery(callbackQuery.getId(), answerCallbackQueryText);
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

                keyboard = TagMenu.getTagsPageableButtons(pageTags, pressedButton);
            }

            tgmBot.editMessageText(chatId, messageId, captionMenu, keyboard);
            tgmBot.answerCallbackQuery(callbackQuery.getId(), answerCallbackQueryText);
        }
    }

    private String getUrlFromTgUrl(String tgUrl, String callbackQueryId) {
        URI uri = URI.create(tgUrl);
        return Arrays.stream(uri.getQuery().split("&"))
                .filter(f -> f.startsWith("url="))
                .findFirst()
                .orElseThrow(
                        () -> {
                            tgmBot.answerCallbackQuery(
                                    callbackQueryId,
                                    "Something went wrong 😢");

                            return new IllegalArgumentException("Failed to parse url=" + uri);
                        }
                )
                .replaceFirst("url=", "");
    }

}
