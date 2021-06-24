package ashnext.telegram.api.types;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChatMember {

    private TgmUser user;

    private String status;

    private Integer until_date;
}
