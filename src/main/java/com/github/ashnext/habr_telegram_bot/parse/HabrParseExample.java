package com.github.ashnext.habr_telegram_bot.parse;

import com.github.ashnext.habr_telegram_bot.parse.model.Post;
import com.github.ashnext.habr_telegram_bot.parse.model.nodeTph.NodeElementTph;
import com.github.ashnext.habr_telegram_bot.parse.model.nodeTph.NodeTextTph;
import com.github.ashnext.habr_telegram_bot.parse.model.nodeTph.NodeTph;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.ashnext.habr_telegram_bot.parse.HabrParser.SITE_URL;

public class HabrParseExample {

    private static final int MAX_PAGES = 210;
    private static final int MAX_POSTS = 20;
    private static final String URL_TELEGRAPH = "https://api.telegra.ph/createPage?access_token=b968da509bb76866c35425099bc0989a5ec3b32997d55286c657e6994bbb&title=Sample+Page&return_content=true&author_name=Anonymous&content=[%CONTENT%]";
    private static final String FAKE_POST_URL = SITE_URL + "/ru/company/selectel/blog/555940/";


    private final List<Post> posts = new ArrayList<>();
    private int pageCounter = 0;

    public static void main(String[] args) throws IOException {
        HabrParseExample parseHabr = new HabrParseExample();
        parseHabr.parsePageNew("/ru/all/");

        StringBuilder hubsString = new StringBuilder();
        StringBuilder tagsString = new StringBuilder();

        Set<String> setHubs = new HashSet<>();
        Set<String> setTags = new HashSet<>();

        parseHabr.getPosts().forEach(
                post -> {
                    setHubs.addAll(post.getHubs());
                    try {
                        setTags.addAll(parseHabr.parsePost(post.getUrl()).getTags());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        setHubs.forEach(hub -> hubsString.append("('").append(hub).append("'), "));
        setTags.forEach(hub -> tagsString.append("('").append(hub).append("'), "));

        try(BufferedWriter writer = new BufferedWriter(new FileWriter("D:\\hubs_file.txt", true))) {
            writer.append(hubsString);
        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter("D:\\tags_file.txt", true))) {
            writer.append(tagsString);
        }

        System.out.println("Posts: " + parseHabr.getPosts().size());
        System.out.println("Hubs: " + setHubs.size());
        System.out.println("Tags: " + setTags.size());

//        parseHabr.printPosts();
//
//        Post post = parseHabr.getPosts().get(0);
//
//        CreatePageTph createPageTph = new CreatePageTph(
//                "b968da509bb76866c35425099bc0989a5ec3b32997d55286c657e6994bbb",
//                post.getHeader(),
//                "Anonymous",
//                parseHabr.getNodes(post));
//
//        ObjectMapper mapper = new ObjectMapper();
//        String jsonResult = "";
//        try {
//            jsonResult = mapper
//                    .writeValueAsString(createPageTph);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//        System.out.println(jsonResult);
    }

    private void parsePageNew(String url) throws IOException {
        pageCounter++;

        Document html = Jsoup.connect(SITE_URL + url).get();

        posts.addAll(HabrParser.parsePostsOnNewPage(html));

        if (pageCounter < MAX_PAGES && posts.size() < MAX_POSTS) {
            Element nextPage = html.getElementById("pagination-next-page");
            if (nextPage.hasAttr("href")) {
                parsePageNew(nextPage.attr("href"));
            }
        }
    }

    private Post parsePost(String url) throws IOException {
        Document html = Jsoup.connect(url).get();
        return HabrParser.parseNewPost(html, url);
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
                .toList();

        for (Element contentItem : contentItems) {
            Elements postTitles = contentItem.getElementsByClass("post__title_link");
            if (postTitles.size() == 0) {
                continue;
            }

            Elements postTags = contentItem.getElementsByClass("inline-list__item-link hub-link ");
            List<String> tags = postTags.stream().map(Element::text).collect(Collectors.toList());

            Element postTitle = postTitles.get(0);
//            if (postTitle.hasAttr("href") && postTitle.hasText()) {
////                String postUrl = FAKE_POST_URL;
//                String postUrl = postTitle.attr("href");
//
//                Document postHtml = Jsoup.connect(postUrl).get();
//
//                Elements innerPostTitles = postHtml.getElementsByClass("post__title-text");
//                if (innerPostTitles.size() == 0) {
//                    continue;
//                }
//                Element innerPostTitle = innerPostTitles.get(0);
//                if (!innerPostTitle.hasText()) {
//                    continue;
//                }
//                String title = innerPostTitle.text();
//
//                Elements postTimeElements = postHtml.getElementsByClass("post__time");
//                if (postsListElements.size() == 0) {
//                    continue;
//                }
//                Element postTimeElement = postTimeElements.get(0);
//                if (!postTimeElement.hasAttr("data-time_published")) {
//                    continue;
//                }
//                Instant time = DateTimeUtils
//                        .parseDataTimePublished(postTimeElement.attr("data-time_published"));
//
//                Element contentElement = postHtml.getElementById("post-content-body");
//                if (contentElement == null) {
//                    continue;
//                }
//
////                Elements images = contentElement.getElementsByTag("img");
////                for (Element image : images) {
////                    Element parent = image.parent();
////                    int siblingIndex = image.siblingIndex();
////                    Element element = new Element("p");
////                    element.insertChildren(0, image);
////                    parent.insertChildren(siblingIndex, element);
////                }
//
//                Elements iframes = contentElement.getElementsByTag("iframe");
//                for (Element iframe : iframes) {
//
//                    String hostUrl = "";
//                    String pathUrl = "";
//
//                    if (iframe.hasAttr("src")) {
//                        Document res = Jsoup.connect(iframe.attr("src")).get();
//                        Element habrEmbed = res.getElementById("habr-embed");
//                        if (habrEmbed != null) {
//                            Elements eIframes = habrEmbed.getElementsByTag("iframe");
//                            if (!eIframes.isEmpty()) {
//                                Element eIframe = eIframes.get(0);
//                                String urlVideo = eIframe.attr("src");
//                                URL url1 = new URL(urlVideo);
//                                hostUrl = url1.getHost();
//                                String[] subPaths = url1.getPath().split("/");
//                                if (subPaths.length == 3) {
//                                    pathUrl = subPaths[2];
//                                }
//                            }
//                        }
//                    }
//
//                    Element parent = iframe.parent();
//                    int siblingIndex = iframe.siblingIndex();
//
//                    if (hostUrl.endsWith("youtube.com") && !pathUrl.isBlank()) {
//                        String urlVideo = "https://www.youtube.com/watch?v=" + pathUrl;
//                        iframe.attr("src", "/embed/youtube?url=" + urlVideo);
//
//                        Element figure = new Element("figure");
//                        figure.insertChildren(0, iframe);
//
//                        parent.insertChildren(siblingIndex, figure);
//                    } else {
//                        Element a = new Element("a");
//                        a.attr("href", iframe.attr("src"));
//                        a.appendText("Embedded video");
//                        parent.insertChildren(siblingIndex, a);
//                        parent.insertChildren(siblingIndex + 1, new Element("br"));
//                        iframe.remove();
//                    }
//                    //https://www.youtube.com/watch?v=X-vj5SFf6m0
//                    //https://www.youtube.com/embed/HD5KbeR5mtc?rel=0&showinfo=1
////                    String uri = URLEncoder.encode(urlVideo, StandardCharsets.UTF_8.toString());
//                }
//
//                posts.add(new Post(postUrl, title, time, tags, contentElement.html()));
//                if (posts.size() >= MAX_POSTS) {
//                    return;
//                }
//            }

            posts.add(new Post(null, null, null, null, tags, null));
            if (posts.size() >= MAX_POSTS) {
                return;
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

    public List<Post> getPosts() {
        return new ArrayList<>(posts);
    }

    private List<NodeTph> getNodes(Post post) {
        Document doc = Jsoup.parse(post.getContent());
        Element body = doc.body();
        return getNode(body).getChildren();
    }

    private NodeElementTph getNode(Node node) {
        NodeElementTph result = new NodeElementTph(node.nodeName());

        for (Attribute attr : node.attributes()) {
            result.addAttr(attr.getKey(), attr.getValue());
        }

        for (Node childNode : node.childNodes()) {
            if (childNode.nodeName().equals("#text")) {
                if (childNode.outerHtml().isBlank()) {
                    continue;
                }
                result.addChildren(new NodeTextTph(childNode.outerHtml()));
            } else {
                result.addChildren(getNode(childNode));
            }
        }

        return result;
    }
}
