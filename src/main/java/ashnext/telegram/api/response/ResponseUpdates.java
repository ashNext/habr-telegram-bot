package ashnext.telegram.api.response;

import ashnext.telegram.api.types.Update;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResponseUpdates extends TgmResponse {

    private List<Update> result;
}
