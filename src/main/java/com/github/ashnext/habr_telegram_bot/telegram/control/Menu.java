package com.github.ashnext.habr_telegram_bot.telegram.control;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Menu {
    HB("hb"),
    BM("bm"),
    TG("tg");

    private final String text;

    public static Menu fromString(String text) {
        for (Menu startNameButton : Menu.values()) {
            if (startNameButton.text.equalsIgnoreCase(text)) {
                return startNameButton;
            }
        }

        return null;
    }
}
