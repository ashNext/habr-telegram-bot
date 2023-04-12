package com.github.ashnext.habr_telegram_bot.telegram.api.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class BotCommand {

    private String command;

    private String description;
}
