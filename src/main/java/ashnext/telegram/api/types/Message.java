package ashnext.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Message {

    @JsonProperty("message_id")
    private Integer messageId;

    @JsonProperty("from")
    private TgmUser tgmUser;

    private Integer date;

    private Chat chat;

    private String text;

    private MessageEntity[] entities;

    @JsonProperty("reply_markup")
    private InlineKeyboardMarkup replyMarkup;
}