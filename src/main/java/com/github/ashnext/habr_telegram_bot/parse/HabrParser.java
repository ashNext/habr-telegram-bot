package com.github.ashnext.habr_telegram_bot.parse;

import com.github.ashnext.habr_telegram_bot.parse.model.Post;
import com.github.ashnext.habr_telegram_bot.parse.util.DateTimeUtils;
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

    public static final String SITE_URL = "https://habr.com";

    public static Post parseNewPost(Document postHtml, String postUrl) {
        Element articleElement = getElementByClass(postHtml,
                "tm-article-presenter__content tm-article-presenter__content_narrow");

        Element articleSnippetElement = getElementByClass(articleElement,
                "tm-article-snippet tm-article-presenter__snippet tm-article-snippet");

        //time
        Element datetimePublishedElement = getElementByClass(articleSnippetElement,
                "tm-article-datetime-published");
        Element timeElement = getElementByTag(datetimePublishedElement, "time");
        Instant time = DateTimeUtils.parseDataTimePublished(getAttrElement(timeElement, "datetime"));

        //title
        Element titleElement = getElementByClass(articleSnippetElement, "tm-title tm-title_h1");
        Element spanElement = getElementByTag(titleElement, "span");
        String title = getTextElement(spanElement);

        Element metaElement = getElementByClass(articleElement, "tm-article-presenter__meta");
        //hubs
        List<String> hubs = metaElement.getElementsByClass("tm-hubs-list__link")
                .stream()
                .map(Element::text)
                .toList();

        //tags
        List<String> tags = metaElement.getElementsByClass("tm-tags-list__link")
                .stream()
                .map(Element::text)
                .toList();

        //body
        Element postContentBody = articleElement.getElementById("post-content-body");
        if (postContentBody == null) {
            throw new HabrParserException("No element 'post-content-body'");
        }

        return new Post(postUrl, title, time, hubs, tags, postContentBody.html());
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
                            "tm-title tm-title_h2");
                    // url
                    Element urlElement = getElementByTag(titleAndLinkElement, "a");
                    String url = SITE_URL + getAttrElement(urlElement, "href");
                    //title
                    Element titleElement = getElementByTag(titleAndLinkElement, "span");
                    String title = getTextElement(titleElement);

                    //time
                    Element datetimePublishedElement = getElementByClass(articleSnippetElement,
                            "tm-article-datetime-published");
                    Element timeElement = getElementByTag(datetimePublishedElement, "time");
                    Instant time = DateTimeUtils.parseDataTimePublished(getAttrElement(timeElement, "datetime"));

                    //hubs
                    List<String> hubs = articleSnippetElement.getElementsByClass("tm-article-snippet__hubs-item")
                            .stream()
                            .map(element -> getElementByTag(element, "span").text())
                            .collect(Collectors.toList());

                    return new Post(url, title, time, hubs, null, null);
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
        elements.remove(element);
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
