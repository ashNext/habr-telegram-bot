package habr.telegram.bot.habrtelegrambot.tgmApi.response;

import habr.telegram.bot.habrtelegrambot.tgmApi.types.Message;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResponseMessage extends Response {

    private Message result;
}
