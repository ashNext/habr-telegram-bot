package com.github.ashnext.habr_telegram_bot.telegram.control.bookmark;

import com.github.ashnext.habr_telegram_bot.bookmark.Bookmark;
import com.github.ashnext.habr_telegram_bot.telegram.api.types.InlineKeyboardButton;
import com.github.ashnext.habr_telegram_bot.telegram.api.types.InlineKeyboardMarkup;

import java.util.Arrays;
import java.util.List;

public class BookmarkMenu {

    private BookmarkMenu() {
    }

    public static InlineKeyboardMarkup getButtonsWithAdd() {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton[][]{{
                        new InlineKeyboardButton("\uD83D\uDCE5", BookmarkButton.builder()
                                .actionBookmarkButton(ActionBookmarkButton.PUT)
                                .build().toString(), ""),
                        new InlineKeyboardButton("\uD83D\uDDD1", "delete", "")
                }}
        );
    }

    public static InlineKeyboardMarkup getButtonsWithRemove() {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton[][]{{
                        new InlineKeyboardButton("\uD83D\uDCE4", BookmarkButton.builder()
                                .actionBookmarkButton(ActionBookmarkButton.PULL)
                                .build().toString(), ""),
                        new InlineKeyboardButton("\uD83D\uDDD1", "delete", "")
                }}
        );
    }

    public static BookmarkButton getButton(String data) {
        List<String> dataList = Arrays.stream(data.split(":")).toList();

        return new BookmarkButton(
                ActionBookmarkButton.fromString(dataList.get(1)),
                dataList.size() < 3 ? null : dataList.get(2)
        );
    }

    public static InlineKeyboardMarkup getBookmarkButtons(List<Bookmark> bookmarkList) {
        InlineKeyboardButton[][] buttons = new InlineKeyboardButton[bookmarkList.size() + 1][1];

        for (int i = 0; i < bookmarkList.size(); i++) {
            InlineKeyboardButton button =
                    new InlineKeyboardButton(
                            bookmarkList.get(i).getPostTitle(),
                            BookmarkButton.builder()
                                    .actionBookmarkButton(ActionBookmarkButton.GET)
                                    .data(bookmarkList.get(i).getId().toString())
                                    .build().toString(),
                            "");
            buttons[i][0] = button;
        }

        buttons[bookmarkList.size()][0] = new InlineKeyboardButton("\uD83C\uDD91 Close", "close", "");

        return new InlineKeyboardMarkup(buttons);
    }
}
