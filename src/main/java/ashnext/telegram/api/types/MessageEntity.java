package ashnext.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("user")
    private TgmUser tgmUser;

    private String language;
}
