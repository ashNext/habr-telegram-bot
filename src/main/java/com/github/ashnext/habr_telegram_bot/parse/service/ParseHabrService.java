package com.github.ashnext.habr_telegram_bot.parse.service;

import com.github.ashnext.habr_telegram_bot.parse.HabrParser;
import com.github.ashnext.habr_telegram_bot.parse.HabrParserException;
import com.github.ashnext.habr_telegram_bot.parse.model.Post;
import com.github.ashnext.habr_telegram_bot.tag.Tag;
import com.github.ashnext.habr_telegram_bot.tag.service.TagService;
import com.github.ashnext.habr_telegram_bot.telegram.api.TgmBot;
import com.github.ashnext.habr_telegram_bot.telegram.control.bookmark.BookmarkMenu;
import com.github.ashnext.habr_telegram_bot.user.User;
import com.github.ashnext.habr_telegram_bot.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static com.github.ashnext.habr_telegram_bot.parse.HabrParser.SITE_URL;
import static com.github.ashnext.habr_telegram_bot.telegram.api.TgmBot.TG_INSTANT_VIEW_TEMPLATE;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParseHabrService {

    private static final String FIRST_PAGE_URL = SITE_URL + "/ru/all/";
    private static final String SECOND_PAGE_URL = SITE_URL + "/ru/all/page2/";

    private final TagService tagService;
    private final UserService userService;
    private final TgmBot tgmBot;

    private final Queue<String> previousNewUrlsPosts = new LinkedList<>();

    @Scheduled(fixedDelayString = "${bot.scheduled.new-posts}")
    public void updateUserFeed() {
        log.info("[updateUserFeed] check new posts");
        List<Post> postList = getNewPosts();
        if (!postList.isEmpty()) {
            List<User> userList = userService.getAllWithTagsByActiveAndSub();

            postList.forEach(post ->
                    userList.forEach(
                            user -> {
                                boolean send = false;

                                if (user.getTags().isEmpty() || post.getTags().isEmpty()) {
                                    send = true;
                                } else {
                                    for (Tag userTag : user.getTags()) {
                                        if (post.getTags().contains(userTag.getName())) {
                                            send = true;
                                            break;
                                        }
                                    }
                                }

                                if (send) {
                                    tgmBot.sendMessage(
                                            user.getTelegramChatId(),
                                            String.format(TG_INSTANT_VIEW_TEMPLATE, post.getUrl()),
                                            BookmarkMenu.getButtonsWithAdd());
                                }
                            }
                    )
            );
        }
    }

    private List<Post> getNewPosts() {
        List<Post> newPosts = new LinkedList<>();

        List<Post> currentPostList = parseAndGetPostsOnPage(FIRST_PAGE_URL);
        Collections.reverse(currentPostList);

        if (previousNewUrlsPosts.isEmpty() && !currentPostList.isEmpty()) {
            List<Post> postUrlsPage2 = parseAndGetPostsOnPage(SECOND_PAGE_URL);
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
                log.error("Warning in parseAndGetPost: Failed to parse the post ({}): {}", postUrl, e.getMessage());
                tgmBot.sendServiceMessage("[parseAndGetPost] postUrl=" + postUrl + "e.message=" + e.getMessage());
            }
        } catch (IOException ioException) {
            log.error("Error in parseAndGetPost when getting the document ({}): {}", postUrl, ioException.getMessage(), ioException);
        }

        return Optional.empty();
    }

    private List<Post> parseAndGetPostsOnPage(String pageUrl) {
        try {
            Document html = Jsoup.connect(pageUrl).get();

            try {
                return HabrParser.parsePostsOnNewPage(html);
            } catch (HabrParserException e) {
                log.error("Warning in parseAndGetPostsOnPage: Failed to parse the site ({}): {}", pageUrl, e.getMessage());
                tgmBot.sendServiceMessage("[parseAndGetPostsOnPage] pageUrl=" + pageUrl + "e.message=" + e.getMessage());
            }
        } catch (IOException ioException) {
            log.error("Warning in parseAndGetUrlsOnPage when getting the document ({}): {}", pageUrl, ioException.getMessage(),
                    ioException);
        }

        return List.of();
    }
}
