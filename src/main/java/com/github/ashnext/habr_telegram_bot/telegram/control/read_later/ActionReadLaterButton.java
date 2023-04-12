package com.github.ashnext.habr_telegram_bot.telegram.control.read_later;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ActionReadLaterButton {
    EMPTY(""),
    ALL("all"),
    PUT("put"),
    GET("get"),
    PULL("pull");

    private final String text;

    public static ActionReadLaterButton fromString(String text) {
        for (ActionReadLaterButton actionReadLaterButton : ActionReadLaterButton.values()) {
            if (actionReadLaterButton.text.equalsIgnoreCase(text)) {
                return actionReadLaterButton;
            }
        }

        return null;
    }
}
