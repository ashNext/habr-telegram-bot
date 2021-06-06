package habr.telegram.bot.habrtelegrambot.tgmApi.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Update implements Serializable {

    @JsonProperty("update_id")
    private Integer updateId;

    private Message message;
}
