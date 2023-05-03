package com.github.ashnext.habr_telegram_bot.telegram.control.tag;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum GroupTag {
    EMPTY(""),
    MY_TAGS("my"),
    NEW_TAG("new");

    private final String text;

    public static GroupTag fromString(String text) {
        for (GroupTag groupHub : GroupTag.values()) {
            if (groupHub.text.equalsIgnoreCase(text)) {
                return groupHub;
            }
        }

        return null;
    }
}
