package com.github.ashnext.habr_telegram_bot.user.service;

import com.github.ashnext.habr_telegram_bot.tag.Tag;
import com.github.ashnext.habr_telegram_bot.tag.TagGroup;
import com.github.ashnext.habr_telegram_bot.user.User;
import com.github.ashnext.habr_telegram_bot.tag.service.TagService;
import com.github.ashnext.habr_telegram_bot.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final TagService tagService;

    public User create(User user) {
        user.setActive(true);
        user.setSubscription(false);
        User newUser = userRepository.save(user);
        log.info("Added new user ({})", newUser);
        return newUser;
    }

    public User update(User user) {
        return userRepository.save(user);
    }

    public Optional<Tag> addTagByUserIdAndTagId(UUID userId, UUID tagId) {
        Optional<User> user = getByIdWithTags(userId);
        Optional<Tag> tag = tagService.getById(tagId);

        if (tag.isPresent() && user.isPresent()) {
            List<Tag> userTags = user.get().getTags();
            if (!userTags.contains(tag.get())) {
                user.get().getTags().add(tag.get());
                update(user.get());
                return tag;
            }
        }
        return Optional.empty();
    }

    public Optional<Tag> removeTagByUserIdAndTagId(UUID userId, UUID tagId) {
        Optional<User> user = getByIdWithTags(userId);
        Optional<Tag> tag = tagService.getById(tagId);

        if (tag.isPresent() && user.isPresent()) {
            List<Tag> userTags = user.get().getTags();
            if (userTags.contains(tag.get())) {
                user.get().getTags().remove(tag.get());
                update(user.get());
                return tag;
            }
        }
        return Optional.empty();
    }

    public Page<Tag> getByIdAndTagGroup(UUID userId, TagGroup tagGroup, int page, int size) {
        Optional<User> user = userRepository.findWithTagsByIdAndTagGroup(userId, tagGroup);

        if (user.isPresent()) {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));

            List<Tag> tags = user.get().getTags();

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), tags.size());

            if (tags.subList(start, end).isEmpty()) {
                pageable = PageRequest.of(--page, size, Sort.by(Sort.Direction.ASC, "name"));
                start = (int) pageable.getOffset();
                end = Math.min((start + pageable.getPageSize()), tags.size());
            }

            return new PageImpl<>(
                    tags.subList(start, end),
                    pageable,
                    tags.size());
        }
        return null;
    }

    private Optional<User> getByIdWithTags(UUID id) {
        return userRepository.findByIdWithTags(id);
    }

    public User getByTelegramUserId(Long telegramUserId) {
        return userRepository.findByTelegramUserId(telegramUserId);
    }

    public void setActive(User user, boolean active) {
        user.setActive(active);
        userRepository.save(user);
        log.info("User {}: {}", active ? "enabled" : "disabled", user);
    }

    private void setSubscription(User user, boolean subscribe) {
        user.setSubscription(subscribe);
        userRepository.save(user);
    }

    public void subscribe(User user) {
        setSubscription(user, true);
        log.info("User subscribed: {}", user);
    }

    public void unsubscribe(User user) {
        setSubscription(user, false);
        log.info("User unsubscribed: {}", user);
    }

    public List<User> getAllActiveAndSubscribe() {
        return userRepository.findAllByActiveTrueAndSubscriptionTrue();
    }

    public List<User> getAllWithTagsByActiveAndSub() {
        return userRepository.findAllWithTagsByActiveAndSub();
    }
}