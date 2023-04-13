package com.github.ashnext.habr_telegram_bot.telegram.control.tag;

import com.github.ashnext.habr_telegram_bot.tag.Tag;
import com.github.ashnext.habr_telegram_bot.telegram.api.types.InlineKeyboardButton;
import com.github.ashnext.habr_telegram_bot.telegram.api.types.InlineKeyboardMarkup;
import org.springframework.data.domain.Page;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class TagMenu {

    private TagMenu() {
    }

    public static TagButton getButton(String data) {
        List<String> dataList = Arrays.stream(data.split(":")).toList();

        return new TagButton(
                GroupTag.fromString(dataList.get(1)),
                TypeTag.fromString(dataList.get(2)),
                ActionTagButton.fromString(dataList.get(3)),
                Integer.parseInt(dataList.get(4)),
                dataList.size() < 6 ? null : dataList.get(5)
        );
    }

    public static InlineKeyboardMarkup getTagManagementButtons() {
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

    public static InlineKeyboardMarkup getTagsPageableButtons(Page<Tag> pageTags, TagButton pressedTagButton) {
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
        buttons[kbCountLines][2] = new InlineKeyboardButton("\u21A9", TagButton.newBuilder().setActionManagement().build().toString(), "");
        buttons[kbCountLines][3] = new InlineKeyboardButton("\uD83C\uDD91", "close", "");

        return new InlineKeyboardMarkup(buttons);
    }
}
