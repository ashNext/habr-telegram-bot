package ashnext.telegram.api.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResponseBoolean extends TgmResponse {

    private boolean result;
}
