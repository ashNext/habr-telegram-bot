package ashnext.telegram.tasks;

import ashnext.service.UserService;
import ashnext.telegram.service.ParseHabrService;
import ashnext.telegram.service.TgmBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NewsletterTask {

    private final ParseHabrService parseHabrService;

    private final UserService userService;

    private final TgmBotService tgmBotService;

    @Scheduled(fixedDelay = 120*60000)
    public void newsletter() {
        parseHabrService.getNewPosts().forEach(
                post -> userService.getAllActiveAndSubscribe().forEach(
                        user -> tgmBotService.getTgmBot().sendMessage(user.getTelegramChatId(), post.getUrl())
                )
        );
    }

}
