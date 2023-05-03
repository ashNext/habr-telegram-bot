package com.github.ashnext.habr_telegram_bot.user.service;

import com.github.ashnext.habr_telegram_bot.hub.Hub;
import com.github.ashnext.habr_telegram_bot.hub.HubGroup;
import com.github.ashnext.habr_telegram_bot.user.User;
import com.github.ashnext.habr_telegram_bot.hub.service.HubService;
import com.github.ashnext.habr_telegram_bot.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final HubService hubService;

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

    public Optional<Hub> addHubByUserIdAndHubId(User user, UUID hubId) {
        Optional<Hub> hub = hubService.getById(hubId);

        if (hub.isPresent()) {
            List<Hub> userHubs = user.getHubs();
            if (!userHubs.contains(hub.get())) {
                user.getHubs().add(hub.get());
                update(user);
                return hub;
            }
        }
        return Optional.empty();
    }

    public Optional<Hub> removeHubByUserIdAndHubId(User user, UUID hubId) {
        Optional<Hub> hub = hubService.getById(hubId);

        if (hub.isPresent()) {
            List<Hub> userHubs = user.getHubs();
            if (userHubs.contains(hub.get())) {
                user.getHubs().remove(hub.get());
                update(user);
                return hub;
            }
        }
        return Optional.empty();
    }

    public Optional<String> removeTagByUserAndTagName(User user, String tagName) {
        if (user.getTags().contains(tagName)) {
            user.getTags().remove(tagName);
            userRepository.save(user);
            return Optional.of(tagName);
        } else {
            log.error("[removeTagByUserIdAndTagName] not found tag by name={} for userId={}", tagName, user.getId());
        }

        return Optional.empty();
    }

    public Page<Hub> getPageHubsByIdAndHubGroup(UUID userId, HubGroup hubGroup, int page, int size) {
        Optional<User> user = userRepository.findWithHubsByIdAndHubGroup(userId, hubGroup);

        if (user.isPresent()) {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));

            List<Hub> hubs = user.get().getHubs();

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), hubs.size());

            if (hubs.subList(start, end).isEmpty()) {
                pageable = PageRequest.of(--page, size, Sort.by(Sort.Direction.ASC, "name"));
                start = (int) pageable.getOffset();
                end = Math.min((start + pageable.getPageSize()), hubs.size());
            }

            return new PageImpl<>(
                    hubs.subList(start, end),
                    pageable,
                    hubs.size());
        }
        return null;
    }

    public Page<String> getPageTagsById(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (user.getTags() == null) {
            return null;
        }

        List<String> tags = user.getTags().stream().sorted().toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), tags.size());

        if (tags.subList(start, end).isEmpty()) {
            pageable = PageRequest.of(--page, size);
            start = (int) pageable.getOffset();
            end = Math.min((start + pageable.getPageSize()), tags.size());
        }

        return new PageImpl<>(
                tags.subList(start, end),
                pageable,
                tags.size());
    }

    public List<String> addTags(User user, List<String> tagNames) {
        List<String> tagsToAdd = new ArrayList<>();
        for (String tag: tagNames) {
            if (!user.getTags().contains(tag)){
                tagsToAdd.add(tag);
            }
        }
        user.getTags().addAll(tagsToAdd);
        userRepository.save(user);
        return tagsToAdd;
    }

    private Optional<User> getByIdWithHubs(UUID id) {
        return userRepository.findByIdWithHubs(id);
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

    public List<User> getAllWithHubsByActiveAndSub() {
        return userRepository.findAllWithHubsByActiveAndSub();
    }
}
