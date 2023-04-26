package com.github.ashnext.habr_telegram_bot.telegram.control.hub;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ActionHubButton {
    EMPTY(""),
    MANAGEMENT("man"),
    SHOW("s"),
    ADD("a"),
    REMOVE("r");

    private final String text;

    public static ActionHubButton fromString(String text) {
        for (ActionHubButton actionHubButton : ActionHubButton.values()) {
            if (actionHubButton.text.equalsIgnoreCase(text)) {
                return actionHubButton;
            }
        }

        return null;
    }
}
