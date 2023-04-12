package com.github.ashnext.habr_telegram_bot.telegram.control.tag;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum GroupTag {
    EMPTY(""),
    ALL_TAGS("all"),
    WITHOUT_MY_TAGS("wom"),
    MY_TAGS("my");

    private final String text;

    public static GroupTag fromString(String text) {
        for (GroupTag groupTag : GroupTag.values()) {
            if (groupTag.text.equalsIgnoreCase(text)) {
                return groupTag;
            }
        }

        return null;
    }
}
