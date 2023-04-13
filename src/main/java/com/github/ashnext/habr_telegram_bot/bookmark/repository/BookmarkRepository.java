package com.github.ashnext.habr_telegram_bot.bookmark.repository;

import com.github.ashnext.habr_telegram_bot.bookmark.Bookmark;
import com.github.ashnext.habr_telegram_bot.user.User;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {

    List<Bookmark> findAllByUser(User user);

    List<Bookmark> findAllByUserAndPostUrl(User user, String postUrl);
}
