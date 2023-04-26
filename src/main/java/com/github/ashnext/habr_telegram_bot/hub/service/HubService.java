package com.github.ashnext.habr_telegram_bot.hub.service;

import com.github.ashnext.habr_telegram_bot.hub.Hub;
import com.github.ashnext.habr_telegram_bot.hub.HubGroup;
import com.github.ashnext.habr_telegram_bot.hub.repository.HubRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class HubService {

    private final HubRepository hubRepository;

    public List<Hub> getAll() {
        return hubRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public boolean addIfAbsent(String hubName) {
        if (getByHubName(hubName).isPresent()) {
            log.info("Hub '{}' already exists", hubName);
            return false;
        }
        Hub newHub = create(hubName);
        log.info("Added new hub '{}', group '{}'", newHub.getName(), newHub.getHubGroup());
        return true;
    }

    public Hub create(String hubName) {
        HubGroup hubGroup = HubGroup.COMMON;
        if (hubName.startsWith("Блог компании ")) {
            hubGroup = HubGroup.BLOG;
        }
        return hubRepository.save(new Hub(hubName, hubGroup));
    }

    public Optional<Hub> getById(UUID uuid) {
        return hubRepository.findById(uuid);
    }

    private Optional<Hub> getByHubName(String hub) {
        return hubRepository.findByName(hub);
    }

    public Page<Hub> getAll(int page, int size) {
        return hubRepository
                .findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
    }

    public Page<Hub> getAllByHubGroup(HubGroup hubGroup, int page, int size) {
        return hubRepository.findAllByHubGroupIs(
                hubGroup,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"))
        );
    }

    public Page<Hub> getWithoutUserHubs(UUID userId, HubGroup hubGroup, int page, int size) {
        Page<Hub> hubPage = hubRepository.findWithoutUserHubsByUserIdAndHubGroup(
                userId, hubGroup.name(), PageRequest.of(page, size));
        if (page > hubPage.getTotalPages() - 1) {
            return hubRepository.findWithoutUserHubsByUserIdAndHubGroup(
                    userId, hubGroup.name(), PageRequest.of(--page, size));
        }
        return hubPage;
    }

    public Page<Hub> getAllCommon(int page, int size) {
        return getAllByHubGroup(HubGroup.COMMON, page, size);
    }

    public Page<Hub> getAllBlog(int page, int size) {
        return getAllByHubGroup(HubGroup.BLOG, page, size);
    }
}
