package com.github.ashnext.habr_telegram_bot.tag.repository;

import com.github.ashnext.habr_telegram_bot.tag.Tag;
import com.github.ashnext.habr_telegram_bot.tag.TagGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    Optional<Tag> findByName(String name);

    Page<Tag> findAllByTagGroupIs(TagGroup tagGroup, Pageable pageable);

    @Query(
            value = "SELECT tg.* FROM tag tg LEFT JOIN " +
                    "(SELECT * FROM user_tags u WHERE u.user_id = :userId) ut ON tg.id = ut.tags_id " +
                    "WHERE tg.tag_group = :tagGroup AND ut.user_id IS NULL ORDER BY tg.name ASC",
            countQuery = "SELECT count(tg.*) FROM tag tg LEFT JOIN " +
                    "(SELECT * FROM user_tags u WHERE u.user_id = :userId) ut ON tg.id = ut.tags_id " +
                    "WHERE tg.tag_group = :tagGroup AND ut.user_id IS NULL",
            nativeQuery = true)
    Page<Tag> findWithoutUserTagsByUserIdAndTagGroup(@Param("userId") UUID userId,
                                                     @Param("tagGroup") String tagGroup,
                                                     Pageable pageable);
}
