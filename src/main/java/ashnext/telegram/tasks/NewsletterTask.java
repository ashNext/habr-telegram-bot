package ashnext.telegram.tasks;

import ashnext.service.UserService;
import ashnext.telegram.api.types.InlineKeyboardButton;
import ashnext.telegram.api.types.InlineKeyboardMarkup;
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

    private final InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(
            new InlineKeyboardButton[][]
                    {
                            {
                                    new InlineKeyboardButton("Read later", "read-later", ""),
                                    new InlineKeyboardButton("Not interested", "delete", "")
                            }
                    }
    );

    @Scheduled(fixedDelayString = "${bot.scheduled.new-posts}")
    public void newsletter() {
        parseHabrService.getNewPosts().forEach(
                post -> userService.getAllActiveAndSubscribe().forEach(
                        user -> tgmBotService.getTgmBot().sendMessage(
                                user.getTelegramChatId(),
                                post.getUrl(),
                                inlineKeyboardMarkup)
                )
        );
    }

}
