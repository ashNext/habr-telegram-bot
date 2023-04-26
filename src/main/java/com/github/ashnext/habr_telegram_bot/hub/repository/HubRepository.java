package com.github.ashnext.habr_telegram_bot.hub.repository;

import com.github.ashnext.habr_telegram_bot.hub.Hub;
import com.github.ashnext.habr_telegram_bot.hub.HubGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HubRepository extends JpaRepository<Hub, UUID> {

    Optional<Hub> findByName(String name);

    Page<Hub> findAllByHubGroupIs(HubGroup hubGroup, Pageable pageable);

    @Query(
            value = "SELECT hb.* FROM hub hb LEFT JOIN " +
                    "(SELECT * FROM user_hub u WHERE u.user_id = :userId) uh ON hb.id = uh.hub_id " +
                    "WHERE hb.hub_group = :hubGroup AND uh.user_id IS NULL ORDER BY hb.name ASC",
            countQuery = "SELECT count(hb.*) FROM hub hb LEFT JOIN " +
                    "(SELECT * FROM user_hub u WHERE u.user_id = :userId) uh ON hb.id = uh.hub_id " +
                    "WHERE hb.hub_group = :hubGroup AND uh.user_id IS NULL",
            nativeQuery = true)
    Page<Hub> findWithoutUserHubsByUserIdAndHubGroup(@Param("userId") UUID userId,
                                                     @Param("hubGroup") String hubGroup,
                                                     Pageable pageable);
}
