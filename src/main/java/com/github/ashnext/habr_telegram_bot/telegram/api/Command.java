package com.github.ashnext.habr_telegram_bot.telegram.api;

public enum Command {
    START, SUB, UNSUB, BOOKMARKS, HUBS;

    public String getCommand() {
        return "/" + this.name().toLowerCase();
    }

    public static Command valueOfCommand(String value) {
        return Command.valueOf((value.startsWith("/") ? value.substring(1) : value).toUpperCase());
    }
}
