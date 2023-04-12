package com.github.ashnext.habr_telegram_bot.parse.model;

import com.github.ashnext.habr_telegram_bot.parse.model.nodeTph.NodeTph;
import com.github.ashnext.habr_telegram_bot.parse.model.nodeTph.ObjTph;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CreatePageTph implements ObjTph {

    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("title")
    private String title;
    @JsonProperty("author_name")
    private String authorName;
    @JsonProperty("author_url")
    private String authorUrl;
    @JsonProperty("content")
    private List<NodeTph> content;
    @JsonProperty("return_content")
    private boolean returnContent;

    public CreatePageTph(String accessToken, String title, String authorName, List<NodeTph> content, boolean returnContent) {
        this(accessToken, title, authorName, null, content, returnContent);
    }

    public CreatePageTph(String accessToken, String title, String authorName, String authorUrl, List<NodeTph> content) {
        this(accessToken, title, authorName, authorUrl, content, true);
    }

    public CreatePageTph(String accessToken, String title, String authorName, List<NodeTph> content) {
        this(accessToken, title, authorName, null, content, true);
    }
}
