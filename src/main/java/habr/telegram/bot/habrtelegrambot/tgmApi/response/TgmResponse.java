package habr.telegram.bot.habrtelegrambot.tgmApi.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import habr.telegram.bot.habrtelegrambot.tgmApi.types.ResponseParameters;
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
