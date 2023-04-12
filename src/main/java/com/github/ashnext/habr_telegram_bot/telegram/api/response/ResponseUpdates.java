package com.github.ashnext.habr_telegram_bot.telegram.api.response;

import com.github.ashnext.habr_telegram_bot.telegram.api.types.Update;
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
