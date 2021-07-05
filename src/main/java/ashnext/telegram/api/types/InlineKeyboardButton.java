package ashnext.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class InlineKeyboardButton {

    private String text;

    @JsonProperty("callback_data")
    private String callbackData;

    private String url;
}
