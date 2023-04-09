package ashnext.telegram.tasks;

import ashnext.model.Tag;
import ashnext.model.User;
import ashnext.parse.model.Post;
import ashnext.service.UserService;
import ashnext.telegram.control.read_later.ReadLaterMenu;
import ashnext.telegram.service.ParseHabrService;
import ashnext.telegram.service.TgmBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class NewsletterTask {

    private final ParseHabrService parseHabrService;

    private final UserService userService;

    private final TgmBotService tgmBotService;

    @Scheduled(fixedDelayString = "${bot.scheduled.new-posts}")
    public void newsletter() {
        log.info("[newsletter] check new posts");
        List<Post> postList = parseHabrService.getNewPosts();
        if (!postList.isEmpty()) {
            List<User> userList = userService.getAllWithTagsByActiveAndSub();

            postList.forEach(post ->
                    userList.forEach(user -> {
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
                                    tgmBotService.getTgmBot().sendMessage(
                                            user.getTelegramChatId(),
                                            post.getUrl(),
                                            ReadLaterMenu.getButtonsWithAdd());
                                }
                            }
                    )
            );
        }
    }
}
