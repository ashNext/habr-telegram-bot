package com.github.ashnext.habr_telegram_bot.telegram.service;

import com.github.ashnext.habr_telegram_bot.bookmark.Bookmark;
import com.github.ashnext.habr_telegram_bot.bookmark.service.BookmarkService;
import com.github.ashnext.habr_telegram_bot.parse.model.Post;
import com.github.ashnext.habr_telegram_bot.parse.service.ParseHabrService;
import com.github.ashnext.habr_telegram_bot.hub.Hub;
import com.github.ashnext.habr_telegram_bot.hub.HubGroup;
import com.github.ashnext.habr_telegram_bot.hub.service.HubService;
import com.github.ashnext.habr_telegram_bot.telegram.api.Command;
import com.github.ashnext.habr_telegram_bot.telegram.api.TgmBot;
import com.github.ashnext.habr_telegram_bot.telegram.api.types.*;
import com.github.ashnext.habr_telegram_bot.telegram.control.bookmark.BookmarkButton;
import com.github.ashnext.habr_telegram_bot.telegram.control.bookmark.BookmarkMenu;
import com.github.ashnext.habr_telegram_bot.telegram.control.hub.*;
import com.github.ashnext.habr_telegram_bot.telegram.control.tag.ActionTagButton;
import com.github.ashnext.habr_telegram_bot.telegram.control.tag.TagButton;
import com.github.ashnext.habr_telegram_bot.telegram.control.tag.TagMenu;
import com.github.ashnext.habr_telegram_bot.user.User;
import com.github.ashnext.habr_telegram_bot.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;

import static com.github.ashnext.habr_telegram_bot.telegram.api.TgmBot.TG_INSTANT_VIEW_TEMPLATE;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateHandlingService {

    private final TgmBot tgmBot;

    private final UserService userService;

    private final BookmarkService bookmarkService;

    private final ParseHabrService parseHabrService;

    private final HubService hubService;

    private final Map<UUID, Boolean> waitingTagInputToUser = new HashMap<>();

    public void processMessage(Message message) {
        final String firstName = message.getTgmUser().getFirstName();
        final Long tgmUserId = message.getTgmUser().getId();

        String msg;
        InlineKeyboardMarkup buttons = new InlineKeyboardMarkup();

        User user = userService.getByTelegramUserId(tgmUserId);

        String text = message.getText();
        Command command = Command.valueOfCommand(text);

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
                case HUBS -> {
                    tgmBot.deleteMessage(message.getChat().getId(), message.getMessageId());
                    buttons = HubMenu.getHubManagementButtons();

                    msg = "Hubs management:";
                    if (buttons.getInlineKeyboard().length == 0) {
                        msg = msg + " empty";
                    }
                }
                case TAGS -> {
                    tgmBot.deleteMessage(message.getChat().getId(), message.getMessageId());
                    buttons = TagMenu.getTagManagementButtons();

                    msg = "Tags management:";
                    if (buttons.getInlineKeyboard().length == 0) {
                        msg = msg + " empty";
                    }
                }
                default -> {
                    if (waitingTagInputToUser.get(user.getId()) != null && waitingTagInputToUser.get(user.getId())) {
                        msg = "Something is wrong";
                        if (!text.startsWith("/")) {
                            List<String> tags = Arrays.stream(text.split(",")).distinct().toList();
                            userService.addTags(user, tags);
                            msg = "Tags " + tags + " successfully added";
                        }
                    } else {
                        msg = "I don't understand yet ((";
                        log.warn("Unprocessed user (tgmUserId={}) message '{}'", tgmUserId, text);
                    }
                }
            }

            if (waitingTagInputToUser.get(user.getId()) != null && waitingTagInputToUser.get(user.getId())) {
                waitingTagInputToUser.put(user.getId(), false);
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
        final long chatId = cbqMessage.getChat().getId();
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
        } else if (cbqData.startsWith("hb:")) {
            String answerCallbackQueryText = "";
            String captionMenu;
            InlineKeyboardMarkup keyboard;

            HubButton pressedButton = HubMenu.getButton(cbqData);

            if (pressedButton.getActionHubButton() == ActionHubButton.MANAGEMENT) {
                captionMenu = "Hubs management";
                keyboard = HubMenu.getHubManagementButtons();
            } else {
                Page<Hub> pageHubs = null;
                int page = pressedButton.getPage();

                switch (pressedButton.getActionHubButton()) {
                    case ADD -> {
                        Optional<Hub> addedHub = userService
                                .addHubByUserIdAndHubId(user, UUID.fromString(pressedButton.getData()));

                        answerCallbackQueryText = addedHub
                                .map(hub -> "Hab " + hub.getName() + " added")
                                .orElse("The hab has already been added");
                        if (pressedButton.getGroupHub() == GroupHub.ALL_HUBS) {
                            tgmBot.answerCallbackQuery(callbackQuery.getId(), answerCallbackQueryText);
                            return;
                        }
                    }
                    case REMOVE -> {
                        Optional<Hub> removedHub = userService
                                .removeHubByUserIdAndHubId(user, UUID.fromString(pressedButton.getData()));

                        answerCallbackQueryText = removedHub
                                .map(hub -> "Hub " + hub.getName() + " removed")
                                .orElse("The hub has already been removed");
                    }
                }

                HubGroup hubGroup;
                if (pressedButton.getTypeHub() == TypeHub.BLOG) {
                    captionMenu = "company blogs";
                    hubGroup = HubGroup.BLOG;
                } else {
                    captionMenu = "common";
                    hubGroup = HubGroup.COMMON;
                }

                switch (pressedButton.getGroupHub()) {
                    case ALL_HUBS -> {
                        captionMenu = "All " + captionMenu + "\n(click to add)";
                        pageHubs = hubService.getAllByHubGroup(hubGroup, page, 20);
                    }
                    case WITHOUT_MY_HUBS -> {
                        captionMenu = "Without my " + captionMenu + "\n(click to add)";
                        pageHubs = hubService.getWithoutUserHubs(user.getId(), hubGroup, page, 20);
                    }
                    case MY_HUBS -> {
                        captionMenu = "My " + captionMenu + "\n(click to remove)";
                        pageHubs = userService.getPageHubsByIdAndHubGroup(user.getId(), hubGroup, page, 20);
                    }
                }

                if (pageHubs != null && pageHubs.hasContent()) {
                    page = pageHubs.getNumber();
                    captionMenu = captionMenu + " [page " + (page + 1) + " of " + pageHubs.getTotalPages() + "]";
                } else {
                    captionMenu = captionMenu + " [empty]";
                }

                keyboard = HubMenu.getHubsPageableButtons(pageHubs, pressedButton);
            }

            tgmBot.editMessageText(chatId, messageId, captionMenu, keyboard);
            tgmBot.answerCallbackQuery(callbackQuery.getId(), answerCallbackQueryText);
        } else if (cbqData.startsWith("tg:")) {
            String answerCallbackQueryText = "";
            String captionMenu;
            InlineKeyboardMarkup keyboard;

            TagButton pressedButton = TagMenu.getButton(cbqData);

            if (pressedButton.getActionTagButton() == ActionTagButton.MANAGEMENT) {
                captionMenu = "Tags management";
                keyboard = TagMenu.getTagManagementButtons();
            } else {
                Page<String> pageTags = null;
                int page = pressedButton.getPage();

                switch (pressedButton.getActionTagButton()) {
                    case NEW -> {
                        tgmBot.sendMessage(chatId, "Enter tag(s) to add:\ntag1[,tag2,tag3]");
                        tgmBot.answerCallbackQuery(callbackQuery.getId(), answerCallbackQueryText);
                        waitingTagInputToUser.put(user.getId(), true);
                        return;
                    }
                    case REMOVE -> {
                        Optional<String> removedTag = userService
                                .removeTagByUserAndTagName(user, pressedButton.getData());

                        answerCallbackQueryText = removedTag
                                .map(tag -> "Tag " + tag + " removed")
                                .orElse("The tag has already been removed");
                    }
                }

                captionMenu = "My tags";

                switch (pressedButton.getGroupTag()) {
                    case MY_TAGS -> {
                        captionMenu = captionMenu + "\n(click to remove)";
                        pageTags = userService.getPageTagsById(user, page, 20);
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
