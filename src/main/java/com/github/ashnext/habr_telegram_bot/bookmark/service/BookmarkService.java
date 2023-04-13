package com.github.ashnext.habr_telegram_bot.bookmark.service;

import com.github.ashnext.habr_telegram_bot.bookmark.Bookmark;
import com.github.ashnext.habr_telegram_bot.user.User;
import com.github.ashnext.habr_telegram_bot.bookmark.repository.BookmarkRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;

    public Bookmark create(Bookmark bookmark) {
        return bookmarkRepository.save(bookmark);
    }

    public List<Bookmark> getAllByUser(User user) {
        return bookmarkRepository.findAllByUser(user);
    }

    public Optional<Bookmark> getByUUID(UUID uuid) {
        return bookmarkRepository.findById(uuid);
    }

    public List<Bookmark> getAllByUserAndPostUrl(User user, String postUrl) {
        return bookmarkRepository.findAllByUserAndPostUrl(user, postUrl);
    }

    public void delete(UUID uuid) {
        bookmarkRepository.deleteById(uuid);
    }
}
