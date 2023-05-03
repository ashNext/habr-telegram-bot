package com.github.ashnext.habr_telegram_bot.telegram.api;

public enum Command {
    UNKNOWN, START, SUB, UNSUB, BOOKMARKS, HUBS, TAGS;

    public String getCommand() {
        return "/" + this.name().toLowerCase();
    }

    public static Command valueOfCommand(String value) {
        return value.startsWith("/") ? Command.valueOf(value.substring(1).toUpperCase()) : UNKNOWN;
    }
}
