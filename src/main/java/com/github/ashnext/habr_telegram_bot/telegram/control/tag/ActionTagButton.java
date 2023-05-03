package com.github.ashnext.habr_telegram_bot.telegram.control.tag;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ActionTagButton {
    EMPTY(""),
    MANAGEMENT("man"),
    SHOW("s"),
    NEW("n"),
    REMOVE("r");

    private final String text;

    public static ActionTagButton fromString(String text) {
        for (ActionTagButton actionHubButton : ActionTagButton.values()) {
            if (actionHubButton.text.equalsIgnoreCase(text)) {
                return actionHubButton;
            }
        }

        return null;
    }
}
