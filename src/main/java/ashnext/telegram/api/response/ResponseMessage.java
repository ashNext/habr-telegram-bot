package ashnext.telegram.api.response;

import ashnext.telegram.api.types.Message;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResponseMessage extends TgmResponse {

    private Message result;
}
