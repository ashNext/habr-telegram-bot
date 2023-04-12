package com.github.ashnext.habr_telegram_bot.telegram.control.tag;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TypeTag {
    EMPTY(""),
    COMMON("c"),
    BLOG("b");

    private final String text;

    public static TypeTag fromString(String text) {
        for (TypeTag typeTag : TypeTag.values()) {
            if (typeTag.text.equalsIgnoreCase(text)) {
                return typeTag;
            }
        }

        return null;
    }
}
