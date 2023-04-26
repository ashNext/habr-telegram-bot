package com.github.ashnext.habr_telegram_bot.telegram.control.hub;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TypeHub {
    EMPTY(""),
    COMMON("c"),
    BLOG("b");

    private final String text;

    public static TypeHub fromString(String text) {
        for (TypeHub typeHub : TypeHub.values()) {
            if (typeHub.text.equalsIgnoreCase(text)) {
                return typeHub;
            }
        }

        return null;
    }
}
