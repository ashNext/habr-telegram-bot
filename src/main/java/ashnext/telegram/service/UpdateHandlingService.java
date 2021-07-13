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
                buttons = getReadLaterButtons(user);

                msg = "List Read later:";
                if (buttons.getInlineKeyboard().length == 0) {
                    msg = msg + " empty";
                }
            } else if (message.getText().equalsIgnoreCase("/tags")) {
                buttons = getTagButtons();

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
        } else if (cbqData.startsWith("tg:")) {
            Optional<Tag> addedTag = userService
                    .addTagByUserIdAndTagId(user.getId(), UUID.fromString(cbqData.substring(3)));

            if (addedTag.isPresent()) {
                tgmBotService.getTgmBot()
                        .answerCallbackQuery(callbackQuery.getId(), "Tag " + addedTag.get().getName() + " added");
            } else {
                tgmBotService.getTgmBot().answerCallbackQuery(callbackQuery.getId(), "");
            }
        } else if (cbqData.startsWith("tags-")) {
            tgmBotService.getTgmBot().deleteMessage(cbqMessage.getChat().getId(), cbqMessage.getMessageId());
            if (cbqData.equalsIgnoreCase("tags-menu")) {
                tgmBotService.getTgmBot().sendMessage(
                        cbqMessage.getChat().getId(),
                        "Tag management:",
                        getTagButtons());
            } else {
                List<String> dataList = Arrays.stream(cbqData.split(":")).toList();
                String data = dataList.get(0);
                int page = Integer.parseInt(dataList.get(1));

                String msg = "";
                Page<Tag> pageTags = null;
                String answerPrefixCallbackQuery = "tg";
                switch (data) {
                    case "tags-all-tag" -> {
                        pageTags = tagService.getAllByTagGroup(TagGroup.COMMON, page, 20);
                        msg = "Tags Only";
                    }
                    case "tags-all-blog" -> {
                        msg = "Tags Blog";
                        pageTags = tagService.getAllByTagGroup(TagGroup.BLOG, page, 20);
                    }
                    case "tags-my-tag" -> {
                        msg = "My tags Only";
                        pageTags = userService.getByIdAndTagGroup(user.getId(), TagGroup.COMMON, page, 20);
                        answerPrefixCallbackQuery = "tgr";
                    }
                    case "tags-my-blog" -> {
                        msg = "My tags Blog";
                        pageTags = userService.getByIdAndTagGroup(user.getId(), TagGroup.BLOG, page, 20);
                        answerPrefixCallbackQuery = "tgr";
                    }
                }

                if (pageTags != null && pageTags.hasContent()) {
                    msg = msg + " [page " + (page + 1) + " of " + pageTags.getTotalPages() + "]";
                } else {
                    msg = msg + " [empty]";
                }
                tgmBotService.getTgmBot().sendMessage(
                        cbqMessage.getChat().getId(),
                        msg,
                        getTagsButtons(pageTags, page, answerPrefixCallbackQuery, data));
            }
            tgmBotService.getTgmBot().answerCallbackQuery(callbackQuery.getId(), "");
        }
    }

    private InlineKeyboardMarkup getReadLaterButtons(User user) {
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

    private InlineKeyboardMarkup getTagsButtons(Page<Tag> pageTags, int page,
                                                String answerPrefixCallbackQuery, String buttonPrefixCallbackQuery) {
        List<Tag> tags;
        if (pageTags != null && pageTags.hasContent()) {
            tags = pageTags.getContent();
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
                        new InlineKeyboardButton(buttonCaption, answerPrefixCallbackQuery + ":" + tag.getId(), "");
                buttons[i][j] = button;
            }
        }

        String callbackData = buttonPrefixCallbackQuery + ":";
        buttons[kbCountLines] = new InlineKeyboardButton[6];
        buttons[kbCountLines][0] = new InlineKeyboardButton(
                page == 0 ? "" : "<<", callbackData + 0, "");
        buttons[kbCountLines][1] = new InlineKeyboardButton(
                page == 0 ? "" : "<", callbackData + (page - 1), "");
        buttons[kbCountLines][2] = new InlineKeyboardButton("Back", "tags-menu", "");
        buttons[kbCountLines][3] = new InlineKeyboardButton("Close", "delete", "");
        buttons[kbCountLines][4] = new InlineKeyboardButton(
                page == pageTags.getTotalPages() - 1 ? "" : ">", callbackData + (page + 1), "");
        buttons[kbCountLines][5] = new InlineKeyboardButton(
                page == pageTags.getTotalPages() - 1 ? "" : ">>",
                callbackData + (pageTags.getTotalPages() - 1), "");

        return new InlineKeyboardMarkup(buttons);
    }

    private InlineKeyboardMarkup getTagButtons() {
        InlineKeyboardButton[][] buttons = new InlineKeyboardButton[][]{{
                new InlineKeyboardButton("Tags Only", "tags-all-tag:0", ""),
                new InlineKeyboardButton("Tags Blogs", "tags-all-blog:0", ""),
        }, {
                new InlineKeyboardButton("Tags Only with out my", "tags-wom-tag", ""),
                new InlineKeyboardButton("Tags Blogs with out my", "tags-wom-blog", ""),
        }, {
                new InlineKeyboardButton("My tags Only", "tags-my-tag:0", ""),
                new InlineKeyboardButton("My tags Blogs", "tags-my-blog:0", "")
        }};

        return new InlineKeyboardMarkup(buttons);
    }
}
