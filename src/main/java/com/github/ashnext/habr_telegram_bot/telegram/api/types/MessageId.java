package com.github.ashnext.habr_telegram_bot.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MessageId {

    @JsonProperty("message_id")
    private Integer messageId;
}
