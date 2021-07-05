package ashnext.telegram.api.response;

import ashnext.telegram.api.types.MessageId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResponseMessageId extends TgmResponse {

    private MessageId result;
}
