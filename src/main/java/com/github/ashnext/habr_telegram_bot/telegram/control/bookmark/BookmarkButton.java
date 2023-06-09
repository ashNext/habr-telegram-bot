package com.github.ashnext.habr_telegram_bot.telegram.control.bookmark;

import com.github.ashnext.habr_telegram_bot.telegram.control.Menu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookmarkButton {

    private final Menu bmMenu = Menu.BM;
    private ActionBookmarkButton actionBookmarkButton;
    @Builder.Default
    private String data = "";

    @Override
    public String toString() {
        return bmMenu.getText()
                + ":" + actionBookmarkButton.getText()
                + ":" + data;
    }
}
