package com.github.ashnext.habr_telegram_bot.parse.model.nodeTph;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
@JsonSerialize(using = NodeTextTphSerializer.class)
public class NodeTextTph extends NodeTph {

    private String content;
}
