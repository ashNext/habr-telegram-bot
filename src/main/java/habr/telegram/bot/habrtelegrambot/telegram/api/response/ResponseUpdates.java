package habr.telegram.bot.habrtelegrambot.telegram.api.response;

import habr.telegram.bot.habrtelegrambot.telegram.api.types.Update;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResponseUpdates extends TgmResponse {

    private List<Update> result;
}
