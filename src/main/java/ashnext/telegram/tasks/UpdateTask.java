package ashnext.telegram.tasks;

import ashnext.telegram.api.response.ResponseUpdates;
import ashnext.telegram.api.types.Update;
import ashnext.telegram.service.TgmBotService;
import ashnext.telegram.service.UpdateHandlingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateTask {

    private int updateId = 0;

    private final TgmBotService tgmBotService;

    private final UpdateHandlingService updateHandlingService;

    public void defineUpdate(Update update) {
        if (update.getMyChatMember() != null) {
            updateHandlingService.changeUserStatus(update.getMyChatMember());
        } else if (update.getMessage() != null) {
            updateHandlingService.processMessage(update.getMessage());
        } else if (update.getCallbackQuery() != null) {
            updateHandlingService.handlingCallBackQuery(update.getCallbackQuery());
        }
    }

    @Scheduled(fixedDelayString = "${bot.scheduled.update}")
    public void update() {
        Optional<ResponseUpdates> optResponseUpdates =
                tgmBotService.getTgmBot().getUpdates(updateId + 1, 100);

        optResponseUpdates.ifPresent(responseUpdates -> {
            for (Update update : responseUpdates.getResult()) {
                updateId = update.getUpdateId();

                defineUpdate(update);
            }
        });
    }
}
