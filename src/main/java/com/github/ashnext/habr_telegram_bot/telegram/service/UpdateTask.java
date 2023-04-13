package com.github.ashnext.habr_telegram_bot.telegram.service;

import com.github.ashnext.habr_telegram_bot.telegram.api.TgmBot;
import com.github.ashnext.habr_telegram_bot.telegram.api.response.ResponseUpdates;
import com.github.ashnext.habr_telegram_bot.telegram.api.types.Update;
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

    private final TgmBot tgmBot;

    private final UpdateHandlingService updateHandlingService;

    private void defineUpdate(Update update) {
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
                tgmBot.getUpdates(updateId + 1, 100);

        optResponseUpdates.ifPresent(responseUpdates -> {
            for (Update update : responseUpdates.getResult()) {
                updateId = update.getUpdateId();

                defineUpdate(update);
            }
        });
    }
}
