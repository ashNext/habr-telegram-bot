package com.github.ashnext.habr_telegram_bot.bookmark.repository;

import com.github.ashnext.habr_telegram_bot.bookmark.ReadLater;
import com.github.ashnext.habr_telegram_bot.user.User;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadLaterRepository extends JpaRepository<ReadLater, UUID> {

    List<ReadLater> findAllByUser(User user);

    List<ReadLater> findAllByUserAndPostUrl(User user, String postUrl);
}
