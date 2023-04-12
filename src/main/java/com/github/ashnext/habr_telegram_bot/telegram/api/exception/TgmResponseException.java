package com.github.ashnext.habr_telegram_bot.telegram.api.exception;

import com.github.ashnext.habr_telegram_bot.telegram.api.response.TgmResponse;
import lombok.Getter;

@Getter
public class TgmResponseException extends TgmException {

    private TgmResponse response;

    public TgmResponseException(String message) {
        super(message);
    }

    public TgmResponseException(TgmResponse response) {
        this("Error receiving response", response);
        this.response = response;
    }

    public TgmResponseException(String message, TgmResponse response) {
        this(String.format("%s - error_code=%s, description=%s", message, response.getErrorCode(), response.getDescription()));
        this.response = response;
    }
}
