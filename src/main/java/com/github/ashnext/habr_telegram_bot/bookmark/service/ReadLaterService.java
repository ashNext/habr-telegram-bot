package com.github.ashnext.habr_telegram_bot.bookmark.service;

import com.github.ashnext.habr_telegram_bot.bookmark.ReadLater;
import com.github.ashnext.habr_telegram_bot.user.User;
import com.github.ashnext.habr_telegram_bot.bookmark.repository.ReadLaterRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ReadLaterService {

    private final ReadLaterRepository readLaterRepository;

    public ReadLater create(ReadLater readLater) {
        return readLaterRepository.save(readLater);
    }

    public List<ReadLater> getAllByUser(User user) {
        return readLaterRepository.findAllByUser(user);
    }

    public Optional<ReadLater> getByUUID(UUID uuid) {
        return readLaterRepository.findById(uuid);
    }

    public List<ReadLater> getAllByUserAndPostUrl(User user, String postUrl) {
        return readLaterRepository.findAllByUserAndPostUrl(user, postUrl);
    }

    public void delete(UUID uuid) {
        readLaterRepository.deleteById(uuid);
    }
}
