package habr.telegram.bot.habrtelegrambot;


import habr.telegram.bot.habrtelegrambot.model.Post;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class HabrParseExample {

  private static final String SITE_URL = "https://habr.com";
  private static final int MAX_PAGES = 4;
  private static final int MAX_POSTS = 75;


  private final List<Post> posts = new LinkedList<>();
  private int pageCounter = 0;

  public static void main(String[] args) throws IOException {

//    String tm = "23 Май 2021 сегодня";
//    System.out.println(tm);
//    System.out.println(System.getProperty("file.encoding"));
//
//    if (true) {
//      return;
//    }

    HabrParseExample parseHabr = new HabrParseExample();
    parseHabr.parsePage("/ru/");
    parseHabr.printPosts();
  }

  private void parsePage(String url) throws IOException {
    pageCounter++;

    Document html = Jsoup.parse(new URL(SITE_URL + url).openStream(), "windows-1251", url);

    Elements postsListElements = html.getElementsByClass("posts_list");
    if (postsListElements.size() == 0) {
      return;
    }

    Elements contentList = postsListElements.get(0).getElementsByClass("content-list__item content-list__item_post shortcuts_item");

    List<Element> contentItems = contentList.stream()
        .filter(element -> element.attr("id").startsWith("post_"))
        .collect(Collectors.toList());

    for (Element contentItem : contentItems) {
      Elements postTitles = contentItem.getElementsByClass("post__title_link");
      if (postTitles.size() == 0) {
        continue;
      }

      String time = contentItem.getElementsByClass("post__time").text();

      // UTF_8
      time = new String(time.getBytes(), StandardCharsets.UTF_8);

      Element postTitle = postTitles.get(0);
      if (postTitle.hasAttr("href") && postTitle.hasText()) {
        posts.add(new Post(postTitle.attr("href"), postTitle.text(), DateTimeUtils.parseDateTime(time)));
        if (posts.size() >= MAX_POSTS) {
          break;
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
