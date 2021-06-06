package habr.telegram.bot.habrtelegrambot.tgmApi.response;

import java.io.Serializable;
import lombok.Setter;
import lombok.ToString;

@Setter
@ToString
public class Response implements Serializable {

    private boolean ok;
}
