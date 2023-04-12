package ashnext.telegram.service;

import ashnext.parse.HabrParser;
import ashnext.parse.HabrParserException;
import ashnext.parse.model.Post;
import ashnext.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParseHabrService {

    private static final String SITE_URL = "https://habr.com";

    private final TagService tagService;

    private final Queue<String> previousNewUrlsPosts = new LinkedList<>();

    public List<Post> getNewPosts() {
        List<Post> newPosts = new LinkedList<>();

        List<Post> currentPostList = parseAndGetPostsOnPage(SITE_URL + "/ru/all/");
        Collections.reverse(currentPostList);

        if (previousNewUrlsPosts.isEmpty() && !currentPostList.isEmpty()) {
            List<Post> postUrlsPage2 = parseAndGetPostsOnPage(SITE_URL + "/ru/all/page2/");
            if (!postUrlsPage2.isEmpty()) {
                Collections.reverse(postUrlsPage2);
                previousNewUrlsPosts.addAll(postUrlsPage2.stream().map(Post::getUrl).toList());
                previousNewUrlsPosts.addAll(currentPostList.stream().map(Post::getUrl).toList());
            } else {
                return newPosts;
            }
        }

        for (Post currentPost : currentPostList) {
            if (!previousNewUrlsPosts.contains(currentPost.getUrl())) {
                parseAndGetPost(currentPost.getUrl()).ifPresent(
                        post -> {
                            post.getTags().forEach(tagService::addIfAbsent);

                            newPosts.add(post);
                            previousNewUrlsPosts.poll();
                            previousNewUrlsPosts.add(post.getUrl());
                        }
                );
            }
        }

        return newPosts;
    }

    public Optional<Post> parseAndGetPost(String postUrl) {
        try {
            Document postHtml = Jsoup.connect(postUrl).get();

            try {
                return Optional.of(HabrParser.parseNewPost(postHtml, postUrl));
            } catch (HabrParserException e) {
                log.warn("Warning in parseAndGetPost: Failed to parse the post ({}): {}", postUrl, e.getMessage());
            }
        } catch (IOException ioException) {
            log.warn("Error in parseAndGetPost when getting the document ({}): {}", postUrl, ioException.getMessage(), ioException);
        }

        return Optional.empty();
    }

    private List<Post> parseAndGetPostsOnPage(String pageUrl) {
        try {
            Document html = Jsoup.connect(pageUrl).get();

            try {
                return HabrParser.parsePostsOnNewPage(html);
            } catch (HabrParserException e) {
                log.warn("Warning in parseAndGetPostsOnPage: Failed to parse the site ({}): {}", pageUrl, e.getMessage());
            }
        } catch (IOException ioException) {
            log.warn("Warning in parseAndGetUrlsOnPage when getting the document ({}): {}", pageUrl, ioException.getMessage(),
                    ioException);
        }

        return List.of();
    }
}
