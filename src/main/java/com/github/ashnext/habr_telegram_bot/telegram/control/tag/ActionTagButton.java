package com.github.ashnext.habr_telegram_bot.telegram.control.tag;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ActionTagButton {
    EMPTY(""),
    MANAGEMENT("man"),
    SHOW("s"),
    ADD("a"),
    REMOVE("r");

    private final String text;

    public static ActionTagButton fromString(String text) {
        for (ActionTagButton actionTagButton : ActionTagButton.values()) {
            if (actionTagButton.text.equalsIgnoreCase(text)) {
                return actionTagButton;
            }
        }

        return null;
    }
}
