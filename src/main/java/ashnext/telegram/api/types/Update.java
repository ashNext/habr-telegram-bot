package ashnext.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Update {

    @JsonProperty("update_id")
    private Integer updateId;

    private Message message;
}
