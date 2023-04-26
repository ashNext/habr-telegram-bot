package com.github.ashnext.habr_telegram_bot.user.repository;

import com.github.ashnext.habr_telegram_bot.hub.HubGroup;
import com.github.ashnext.habr_telegram_bot.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.hubs WHERE u.id=:id")
    Optional<User> findByIdWithHubs(@Param("id") UUID id);

    User findByTelegramUserId(@Param("telegramUserId") Long telegramUserId);

    List<User> findAllByActiveTrueAndSubscriptionTrue();

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.hubs WHERE u.active=true AND u.subscription=true")
    List<User> findAllWithHubsByActiveAndSub();

    @Query("SELECT u FROM User u JOIN FETCH u.hubs h WHERE u.id=:id AND h.hubGroup=:hubGroup")
    Optional<User> findWithHubsByIdAndHubGroup(@Param("id") UUID id, @Param("hubGroup") HubGroup hubGroup);
}
