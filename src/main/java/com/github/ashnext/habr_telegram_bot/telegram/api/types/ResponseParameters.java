package com.github.ashnext.habr_telegram_bot.telegram.api.types;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResponseParameters {

    private Integer migrate_to_chat_id;

    private Integer retry_after;
}
