package habr.telegram.bot.habrtelegrambot;

import habr.telegram.bot.habrtelegrambot.model.Post;
import java.time.Instant;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HabrParseExample {

    private static final String SITE_URL = "https://habr.com";
    private static final int MAX_PAGES = 3;
    private static final int MAX_POSTS = 55;


    private final List<Post> posts = new ArrayList<>();
    private int pageCounter = 0;

    public static void main(String[] args) throws IOException {
        HabrParseExample parseHabr = new HabrParseExample();
        parseHabr.parsePage("/ru/all/");
        parseHabr.printPosts();
    }

    private void parsePage(String url) throws IOException {
        pageCounter++;

        Document html = Jsoup.connect(SITE_URL + url).get();

        Elements postsListElements = html.getElementsByClass("posts_list");
        if (postsListElements.size() == 0) {
            return;
        }

        Elements contentList = postsListElements.get(0)
                .getElementsByClass("content-list__item content-list__item_post shortcuts_item");

        List<Element> contentItems = contentList.stream()
                .filter(element -> element.attr("id").startsWith("post_"))
                .collect(Collectors.toList());

        for (Element contentItem : contentItems) {
            Elements postTitles = contentItem.getElementsByClass("post__title_link");
            if (postTitles.size() == 0) {
                continue;
            }

            Elements postTags = contentItem.getElementsByClass("inline-list__item-link hub-link ");
            List<String> tags = postTags.stream().map(Element::text).collect(Collectors.toList());

            Element postTitle = postTitles.get(0);
            if (postTitle.hasAttr("href") && postTitle.hasText()) {
                String postUrl = postTitle.attr("href");

                Document postHtml = Jsoup.connect(postUrl).get();
                Element postTimeElement = postHtml.getElementsByClass("post__time").get(0);
                Instant time = null;
                if (postTimeElement.hasAttr("data-time_published")) {
                    time = DateTimeUtils
                            .parseDataTimePublished(postTimeElement.attr("data-time_published"));
                }

                posts.add(new Post(postUrl, postTitle.text(), time, tags));
                if (posts.size() >= MAX_POSTS) {
                    return;
                }
            }
        }

        if (pageCounter < MAX_PAGES && posts.size() < MAX_POSTS) {
            Elements pageFooterElements = postsListElements.get(0).getElementsByClass("page__footer");
            if (pageFooterElements.size() == 0) {
                return;
            }

            Element nextPage = pageFooterElements.get(0).getElementById("next_page");
            if (nextPage != null && nextPage.hasAttr("href")) {
                String newUrl = nextPage.attr("href");
                parsePage(newUrl);
            }
        }
    }

    private void printPosts() {
        posts.forEach(System.out::println);
        System.out.println("posts: " + posts.size());
    }
}
