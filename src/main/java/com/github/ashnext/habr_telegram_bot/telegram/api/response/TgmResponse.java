package com.github.ashnext.habr_telegram_bot.telegram.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ashnext.habr_telegram_bot.telegram.api.types.ResponseParameters;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TgmResponse {

    private boolean ok;

    private String description;

    @JsonProperty("error_code")
    private String errorCode;

    private ResponseParameters parameters;
}
