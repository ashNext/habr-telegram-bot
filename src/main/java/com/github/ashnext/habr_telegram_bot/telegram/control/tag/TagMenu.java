package com.github.ashnext.habr_telegram_bot.telegram.control.tag;

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
                ActionTagButton.fromString(dataList.get(2)),
                Integer.parseInt(dataList.get(3)),
                dataList.size() < 5 ? null : dataList.get(4)
        );
    }

    public static InlineKeyboardMarkup getTagManagementButtons() {
        InlineKeyboardButton[][] buttons = new InlineKeyboardButton[][]{{
                new InlineKeyboardButton("All my",
                        TagButton.newBuilder().setGroupMy().setActionShow().build().toString(), ""),
                new InlineKeyboardButton("Add new tag",
                        TagButton.newBuilder().setGroupAdd().setActionAddNew().build().toString(), ""),
        }, {
                new InlineKeyboardButton("\uD83C\uDD91 Close", "close", "")
        }};

        return new InlineKeyboardMarkup(buttons);
    }

    public static InlineKeyboardMarkup getTagsPageableButtons(Page<String> pageTags, TagButton pressedTagButton) {
        ActionTagButton action;
        switch (pressedTagButton.getGroupTag()) {
            case NEW_TAG ->  action = ActionTagButton.NEW;
            case MY_TAGS -> action = ActionTagButton.REMOVE;
            default -> throw new IllegalStateException("Unexpected value: " + pressedTagButton.getGroupTag());
        }

        List<String> tags;
        int page = 0;
        if (pageTags != null && pageTags.hasContent()) {
            tags = pageTags.getContent();
            page = pageTags.getNumber();
        } else {
            tags = List.of();
        }

        int kbCountLines = tags.size() % 2 == 0 ? tags.size() / 2 : tags.size() / 2 + 1;
        InlineKeyboardButton[][] buttons = new InlineKeyboardButton[kbCountLines + 1][];

        Iterator<String> iterator = tags.iterator();
        for (int i = 0; i < kbCountLines; i++) {
            if (i == kbCountLines - 1 && tags.size() % 2 != 0) {
                buttons[i] = new InlineKeyboardButton[1];
            } else {
                buttons[i] = new InlineKeyboardButton[2];
            }

            for (int j = 0; j < 2 && iterator.hasNext(); j++) {
                String tag = iterator.next();

                InlineKeyboardButton button =
                        new InlineKeyboardButton(tag,
                                TagButton.newBuilder()
                                        .setGroup(pressedTagButton.getGroupTag())
                                        .setAction(action)
                                        .setPage(page)
                                        .setData(tag)
                                        .build().toString(),
                                ""
                        );
                buttons[i][j] = button;
            }
        }

        TagButton.Builder controlButtonBuilder = TagButton.newBuilder()
                .setGroup(pressedTagButton.getGroupTag())
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
