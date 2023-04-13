package com.github.ashnext.habr_telegram_bot.user.repository;

import com.github.ashnext.habr_telegram_bot.tag.TagGroup;
import com.github.ashnext.habr_telegram_bot.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.tags WHERE u.id=:id")
    Optional<User> findByIdWithTags(@Param("id") UUID id);

    User findByTelegramUserId(@Param("telegramUserId") Long telegramUserId);

    List<User> findAllByActiveTrueAndSubscriptionTrue();

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.tags WHERE u.active=true AND u.subscription=true")
    List<User> findAllWithTagsByActiveAndSub();

    @Query("SELECT u FROM User u JOIN FETCH u.tags t WHERE u.id=:id AND t.tagGroup=:tagGroup")
    Optional<User> findWithTagsByIdAndTagGroup(@Param("id") UUID id, @Param("tagGroup") TagGroup tagGroup);
}
