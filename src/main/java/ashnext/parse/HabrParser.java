package ashnext.parse;

import ashnext.parse.model.Post;
import ashnext.parse.util.DateTimeUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HabrParser {

    public static Post parseOldPost(Document postHtml, String postUrl) throws HabrParserException {
        Element innerPostTitle = getElementByClass(postHtml, "post__title-text");
        String title = getTextElement(innerPostTitle);

        Elements postTags = postHtml.getElementsByClass("inline-list__item-link hub-link ");
        List<String> tags = postTags.stream().map(Element::text).collect(Collectors.toList());

        Element postTimeElement = getElementByClass(postHtml, "post__time");
        Instant time = DateTimeUtils.parseDataTimePublished(getAttrElement(postTimeElement, "data-time_published"));

        Element contentElement = postHtml.getElementById("post-content-body");
        if (contentElement == null) {
            throw new HabrParserException("No element 'post-content-body'");
        }

        return new Post(postUrl, title, time, tags, contentElement.html());
    }

    public static Post parseNewPost(Document postHtml, String postUrl) {
        Element articleElement = getElementByClass(postHtml,
                "tm-page-article__content tm-page-article__content_inner");

        Element articleSnippetElement = getElementByClass(articleElement,
                "tm-article-snippet tm-page-article__snippet");

        //time
        Element datetimePublishedElement = getElementByClass(articleSnippetElement,
                "tm-article-snippet__datetime-published");
        Element timeElement = getElementByTag(datetimePublishedElement, "time");
        Instant time = DateTimeUtils.parseDataTimePublished(getAttrElement(timeElement, "datetime"));

        //title
        Element titleElement = getElementByClass(articleSnippetElement,
                "tm-article-snippet__title tm-article-snippet__title_h1");
        Element spanElement = getElementByTag(titleElement, "span");
        String title = getTextElement(spanElement);

        //tags
        List<String> tags = articleSnippetElement.getElementsByClass("tm-article-snippet__hubs-item")
                .stream()
                .map(element ->  getElementByTag(element, "span").text())
                .collect(Collectors.toList());

        //body
        Element postContentBody = postHtml.getElementById("post-content-body");
        if (postContentBody == null) {
            throw new HabrParserException("No element 'post-content-body'");
        }

        return new Post(postUrl, title, time, tags, postContentBody.html());
    }

    public static List<Post> parsePostsOnOldPage(Document html) {
        Element postsListElement = getElementByClass(html, "posts_list");

        Elements contentList = postsListElement
                .getElementsByClass("content-list__item content-list__item_post shortcuts_item");

        List<Element> contentItems = contentList.stream()
                .filter(element -> element.attr("id").startsWith("post_")
                        && !element.getElementsByClass("post__title_link").isEmpty())
                .collect(Collectors.toList());

        if (contentItems.isEmpty()) {
            throw new HabrParserException("Element 'content-list__item...' is empty");
        }

        return contentItems.stream()
                .map(contentItem -> {
                    Element postTitleLinkElement = getElementByClass(contentItem, "post__title_link");
                    String postUrl = getAttrElement(postTitleLinkElement, "href");
                    String title = getTextElement(postTitleLinkElement);

                    Element tagsContentElement = getElementByClass(contentItem, "post__hubs inline-list");
                    Elements postTags = tagsContentElement.getElementsByClass("inline-list__item-link hub-link ");
                    List<String> tags = postTags.stream().map(Element::text).collect(Collectors.toList());

                    return new Post(postUrl, title, null, tags, null);
                }).collect(Collectors.toList());
    }

    public static List<Post> parsePostsOnNewPage(Document html) {
        Element articlesListElement = getElementByClass(html, "tm-articles-list");
        Elements articlesElements = articlesListElement.getElementsByClass("tm-articles-list__item");
        if (articlesElements.isEmpty()) {
            throw new HabrParserException(
                    String.format("In element '%s' no elements was found by class '%s'", articlesListElement.nodeName(),
                            "tm-articles-list__item"));
        }

        return articlesElements.stream()
                .filter(element -> !element.getElementsByClass("tm-article-snippet").isEmpty())
                .map(article -> {
                    Element articleSnippetElement = getElementByClass(article, "tm-article-snippet");

                    //title and url
                    Element titleAndLinkElement = getElementByClass(articleSnippetElement,
                            "tm-article-snippet__title-link");
                    // url
                    String url = "https://habr.com" + getAttrElement(titleAndLinkElement, "href");
                    //title
                    Element titleElement = getElementByTag(titleAndLinkElement, "span");
                    String title = getTextElement(titleElement);

                    //time
                    Element datetimePublishedElement = getElementByClass(articleSnippetElement,
                            "tm-article-snippet__datetime-published");
                    Element timeElement = getElementByTag(datetimePublishedElement, "time");
                    Instant time = DateTimeUtils.parseDataTimePublished(getAttrElement(timeElement, "datetime"));

                    //tags
                    List<String> tags = articleSnippetElement.getElementsByClass("tm-article-snippet__hubs-item")
                            .stream()
                            .map(element ->  getElementByTag(element, "span").text())
                            .collect(Collectors.toList());

                    return new Post(url, title, time, tags, null);
                }).collect(Collectors.toList());
    }

    private static Element getElementByClass(Element element, String className) {
        Elements elements = element.getElementsByClass(className);
        if (elements.isEmpty()) {
            throw new HabrParserException(
                    String.format("In element '%s' no element was found by class '%s'", element.nodeName(), className));
        }
        return elements.get(0);
    }

    private static Element getElementByTag(Element element, String tagName) {
        Elements elements = element.getElementsByTag(tagName);
        if (elements.isEmpty()) {
            throw new HabrParserException(
                    String.format("In element '%s' no element was found by tag '%s'", element.nodeName(), tagName));
        }
        return elements.get(0);
    }

    private static String getAttrElement(Element element, String attrName) {
        if (!element.hasAttr(attrName)) {
            throw new HabrParserException(
                    String.format("In element '%s' attribute could not be found '%s'", element.nodeName(), attrName));
        }
        return element.attr(attrName);
    }

    private static String getTextElement(Element element) {
        if (!element.hasText()) {
            throw new HabrParserException(
                    String.format("In element '%s' text could not be found", element.nodeName()));
        }
        return element.text();
    }
}
