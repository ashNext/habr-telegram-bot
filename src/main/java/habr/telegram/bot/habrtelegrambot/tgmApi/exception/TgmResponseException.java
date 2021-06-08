package habr.telegram.bot.habrtelegrambot.tgmApi.exception;

import habr.telegram.bot.habrtelegrambot.tgmApi.response.TgmResponse;
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
