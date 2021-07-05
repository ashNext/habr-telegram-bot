package ashnext.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class InlineKeyboardMarkup {

    @JsonProperty("inline_keyboard")
    private InlineKeyboardButton[][] inlineKeyboard;
}
