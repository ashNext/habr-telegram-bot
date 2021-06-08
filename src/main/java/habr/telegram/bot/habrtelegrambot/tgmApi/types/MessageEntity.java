package habr.telegram.bot.habrtelegrambot.tgmApi.types;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MessageEntity {

    private String type;

    private Integer offset;

    private Integer length;

    private String url;

    private User user;

    private String language;
}
