package ashnext.telegram.service;

import ashnext.model.Tag;
import ashnext.parse.model.Post;
import ashnext.parse.util.DateTimeUtils;
import ashnext.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
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

    private final Queue<String> previousNewPosts = new LinkedList<>();

    public List<Post> getNewPosts() {
        List<Post> newPosts = new LinkedList<>();

        List<String> postUrls = parseAndGetUrlsOnPage(SITE_URL + "/ru/all/");
        Collections.reverse(postUrls);

        if (previousNewPosts.isEmpty() && !postUrls.isEmpty()) {
            List<String> postUrlsPage2 = parseAndGetUrlsOnPage(SITE_URL + "/ru/all/page2/");
            if (!postUrlsPage2.isEmpty()) {
                Collections.reverse(postUrlsPage2);
                previousNewPosts.addAll(postUrlsPage2);
                previousNewPosts.addAll(postUrls);
            } else {
                return newPosts;
            }
        }

        for (String postUrl : postUrls) {
            if (!previousNewPosts.contains(postUrl)) {
                parseAndGetPost(postUrl).ifPresent(post -> {
                    newPosts.add(post);
                    previousNewPosts.poll();
                    previousNewPosts.add(postUrl);

                    post.getTags().forEach(tag -> tagService.create(new Tag(tag)));
                });
            }
        }

        return newPosts;
    }

    public Optional<Post> parseAndGetPost(String postUrl) {
        try {
            Document postHtml = Jsoup.connect(postUrl).get();

            Elements innerPostTitles = postHtml.getElementsByClass("post__title-text");
            if (innerPostTitles.size() < 1 || !innerPostTitles.get(0).hasText()) {
                throw new RuntimeException("No element 'post__title-text' or it does not contain text");
            }
            String title = innerPostTitles.get(0).text();

            Elements postTags = postHtml.getElementsByClass("inline-list__item-link hub-link ");
            List<String> tags = postTags.stream().map(Element::text).collect(Collectors.toList());

            Elements postTimeElements = postHtml.getElementsByClass("post__time");
            if (postTimeElements.size() < 1 || !postTimeElements.get(0).hasAttr("data-time_published")) {
                throw new RuntimeException("No element 'post__time' or it does not contain attribute 'time_published'");
            }
            Instant time = DateTimeUtils
                    .parseDataTimePublished(postTimeElements.get(0).attr("data-time_published"));

            Element contentElement = postHtml.getElementById("post-content-body");
            if (contentElement == null) {
                throw new RuntimeException("No element 'post-content-body'");
            }

            return Optional.of(new Post(postUrl, title, time, tags, contentElement.html()));
        } catch (IOException e) {
            log.warn(String.format("Error in parseAndGetPost when getting the document (%s): ", postUrl), e);
        } catch (Exception e) {
            log.warn(String.format("Error in parseAndGetPost when parse the document (%s): ", postUrl), e);
        }
        return Optional.empty();
    }

    private List<String> parseAndGetUrlsOnPage(String pageUrl) {
        try {
            Document html = Jsoup.connect(pageUrl).get();

            Elements postsListElements = html.getElementsByClass("posts_list");
            if (postsListElements.size() < 1) {
                throw new RuntimeException("No element 'posts_list'");
            }

            Elements contentList = postsListElements.get(0)
                    .getElementsByClass("content-list__item content-list__item_post shortcuts_item");

            List<Element> contentItems = contentList.stream()
                    .filter(element -> element.attr("id").startsWith("post_"))
                    .collect(Collectors.toList());

            if (contentItems.isEmpty()) {
                throw new RuntimeException("Element 'content-list__item...' is empty");
            }

            return contentItems.stream()
                    .map(element -> element.getElementsByClass("post__title_link"))
                    .filter(elements -> !elements.isEmpty())
                    .map(elements -> elements.get(0))
                    .filter(element -> element.hasAttr("href") && element.hasText())
                    .map(element -> element.attr("href"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.warn(String.format("Error in parseAndGetUrlsOnPage when getting the document (%s): ", pageUrl), e);
        } catch (Exception e) {
            log.warn(String.format("Error in parseAndGetUrlsOnPage when parse the document (%s): ", pageUrl), e);
        }
        return List.of();
    }

}
