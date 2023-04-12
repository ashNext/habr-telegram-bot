package com.github.ashnext.habr_telegram_bot.telegram.api.response;

import com.github.ashnext.habr_telegram_bot.telegram.api.types.Message;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResponseMessage extends TgmResponse {

    private Message result;
}
