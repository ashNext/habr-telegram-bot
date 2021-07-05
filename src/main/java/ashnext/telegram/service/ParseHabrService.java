package ashnext.telegram.service;

import ashnext.parse.model.Post;
import ashnext.parse.util.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ParseHabrService {

    private static final String SITE_URL = "https://habr.com";
    private Post lastPost = null;

    @Value("${bot.last-post-url}")
    private String lastPostUrl;

    public List<Post> getNewPosts() {
        List<Post> newPosts = new LinkedList<>();
        try {
            List<String> postUrls = parseAndGetUrlsOnPage(SITE_URL + "/ru/all/");

            if (!postUrls.isEmpty() && lastPostUrl.isBlank()) {
                lastPostUrl = postUrls.get(0);
                return newPosts;
            }

            for (String postUrl : postUrls) {
                if (lastPostUrl.equals(postUrl)) {
                    break;
                }
                parseAndGetPost(postUrl).ifPresent(post -> newPosts.add(0, post));
            }

            if (newPosts.size() > 0) {
                lastPostUrl = newPosts.get(newPosts.size() - 1).getUrl();
            }

        } catch (IOException e) {
            log.error("Error in getNewPosts: ", e);
        }

        return newPosts;
    }

    public Optional<Post> getNewestPost() {
        try {
            List<String> postUrls = parseAndGetUrlsOnPage(SITE_URL + "/ru/all/");
            if (!postUrls.isEmpty()) {
                Optional<Post> optCurrentPost = parseAndGetPost(postUrls.get(0));

                if (optCurrentPost.isPresent()) {
                    if (lastPost == null) {
                        lastPost = optCurrentPost.get();
                    }

                    if (optCurrentPost.get().getUrl().equals(lastPost.getUrl())) {
                        return Optional.empty();
                    } else {
                        lastPost = optCurrentPost.get();
                        log.info("There is a new post");
                        return Optional.of(lastPost);
                    }
                }
            } else {
                log.error("There are no posts!");
            }
        } catch (IOException e) {
            log.error("Error in getNewestPost: ", e);
        }

        return Optional.empty();
    }

    public Optional<Post> parseAndGetPost(String postUrl) throws IOException {
        Document postHtml = Jsoup.connect(postUrl).get();

        Elements innerPostTitles = postHtml.getElementsByClass("post__title-text");
        if (innerPostTitles.size() < 1 || !innerPostTitles.get(0).hasText()) {
            return Optional.empty();
        }
        String title = innerPostTitles.get(0).text();

        Elements postTags = postHtml.getElementsByClass("inline-list__item-link hub-link ");
        List<String> tags = postTags.stream().map(Element::text).collect(Collectors.toList());

        Elements postTimeElements = postHtml.getElementsByClass("post__time");
        if (postTimeElements.size() < 1 || !postTimeElements.get(0).hasAttr("data-time_published")) {
            return Optional.empty();
        }
        Instant time = DateTimeUtils
                .parseDataTimePublished(postTimeElements.get(0).attr("data-time_published"));

        Element contentElement = postHtml.getElementById("post-content-body");
        if (contentElement == null) {
            return Optional.empty();
        }

        return Optional.of(new Post(postUrl, title, time, tags, contentElement.html()));
    }

    private List<String> parseAndGetUrlsOnPage(String pageUrl) throws IOException {

        Document html = Jsoup.connect(pageUrl).get();

        Elements postsListElements = html.getElementsByClass("posts_list");
        if (postsListElements.size() < 1) {
            return List.of();
        }

        Elements contentList = postsListElements.get(0)
                .getElementsByClass("content-list__item content-list__item_post shortcuts_item");

        List<Element> contentItems = contentList.stream()
                .filter(element -> element.attr("id").startsWith("post_"))
                .collect(Collectors.toList());

        return contentItems.stream()
                .map(element -> element.getElementsByClass("post__title_link"))
                .filter(elements -> !elements.isEmpty())
                .map(elements -> elements.get(0))
                .filter(element -> element.hasAttr("href") && element.hasText())
                .map(element -> element.attr("href"))
                .collect(Collectors.toList());
    }

}
