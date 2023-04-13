package com.github.ashnext.habr_telegram_bot.telegram.control.bookmark;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ActionBookmarkButton {
    EMPTY(""),
    ALL("all"),
    PUT("put"),
    GET("get"),
    PULL("pull");

    private final String text;

    public static ActionBookmarkButton fromString(String text) {
        for (ActionBookmarkButton actionBookmarkButton : ActionBookmarkButton.values()) {
            if (actionBookmarkButton.text.equalsIgnoreCase(text)) {
                return actionBookmarkButton;
            }
        }

        return null;
    }
}
