package com.github.ashnext.habr_telegram_bot.telegram.control.hub;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum GroupHub {
    EMPTY(""),
    ALL_HUBS("all"),
    WITHOUT_MY_HUBS("wom"),
    MY_HUBS("my");

    private final String text;

    public static GroupHub fromString(String text) {
        for (GroupHub groupHub : GroupHub.values()) {
            if (groupHub.text.equalsIgnoreCase(text)) {
                return groupHub;
            }
        }

        return null;
    }
}
