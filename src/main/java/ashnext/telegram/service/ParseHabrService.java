package ashnext.telegram.service;

import ashnext.parse.HabrParser;
import ashnext.parse.HabrParserException;
import ashnext.parse.model.Post;
import ashnext.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParseHabrService {

    private static final String SITE_URL = "https://habr.com";

    private final TagService tagService;

    @Value("${bot.last-post-url}")
    private String lastPostUrl;

    private final Queue<String> previousNewUrlsPosts = new LinkedList<>();

    public List<Post> getNewPosts() {
        List<Post> newPosts = new LinkedList<>();

        List<Post> currentPostList = parseAndGetPostsOnPage(SITE_URL + "/ru/all/");
        Collections.reverse(currentPostList);

        if (previousNewUrlsPosts.isEmpty() && !currentPostList.isEmpty()) {
            List<Post> postUrlsPage2 = parseAndGetPostsOnPage(SITE_URL + "/ru/all/page2/");
            if (!postUrlsPage2.isEmpty()) {
                Collections.reverse(postUrlsPage2);
                previousNewUrlsPosts.addAll(postUrlsPage2.stream().map(Post::getUrl).collect(Collectors.toList()));
                previousNewUrlsPosts.addAll(currentPostList.stream().map(Post::getUrl).collect(Collectors.toList()));
            } else {
                return newPosts;
            }
        }

        for (Post currentPost : currentPostList) {
            if (!previousNewUrlsPosts.contains(currentPost.getUrl())) {
                newPosts.add(currentPost);
                previousNewUrlsPosts.poll();
                previousNewUrlsPosts.add(currentPost.getUrl());

                currentPost.getTags().forEach(tagService::create);
            }
        }

        return newPosts;
    }

    public Optional<Post> parseAndGetPost(String postUrl) {
        try {
            Document postHtml = Jsoup.connect(postUrl).get();
            try {
                return Optional.of(HabrParser.parseOldPost(postHtml, postUrl));
            } catch (HabrParserException e) {
                log.warn("Warning in parseAndGetPost: "
                        + "Failed to parse the Old version of the post ({}): ", postUrl);
                try {
                    return Optional.of(HabrParser.parseNewPost(postHtml, postUrl));
                } catch (HabrParserException ee) {
                    log.warn(String.format("Warning in parseAndGetPost: "
                            + "Failed to parse the Old and New versions of the post (%s): ", postUrl), ee);
                }
            }
        } catch (IOException ioException) {
            log.warn(String.format("Error in parseAndGetPost when getting the document (%s): ", postUrl), ioException);
        }

        return Optional.empty();
    }

    private List<Post> parseAndGetPostsOnPage(String pageUrl) {
        try {
            Document html = Jsoup.connect(pageUrl).get();

            try {
                return HabrParser.parsePostsOnOldPage(html);
            } catch (HabrParserException e) {
                log.warn("Warning in parseAndGetPostsOnPage: "
                        + "Failed to parse the Old version of the site ({}): ", pageUrl);
                try {
                    return HabrParser.parsePostsOnNewPage(html);
                } catch (HabrParserException ee) {
                    log.warn(String.format("Warning in parseAndGetPostsOnPage: "
                            + "Failed to parse the Old and New versions of the site (%s): ", pageUrl), ee);
                }
            }

        } catch (IOException ioException) {
            log.warn(String.format("Warning in parseAndGetUrlsOnPage when getting the document (%s): ", pageUrl),
                    ioException);
        }

        return List.of();
    }
}
