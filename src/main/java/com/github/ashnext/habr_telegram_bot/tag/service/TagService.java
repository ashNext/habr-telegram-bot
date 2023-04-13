package com.github.ashnext.habr_telegram_bot.tag.service;

import com.github.ashnext.habr_telegram_bot.tag.Tag;
import com.github.ashnext.habr_telegram_bot.tag.TagGroup;
import com.github.ashnext.habr_telegram_bot.tag.repository.TagRepository;
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
public class TagService {

    private final TagRepository tagRepository;

    public List<Tag> getAll() {
        return tagRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public boolean addIfAbsent(String tagName) {
        if (getByTagName(tagName).isPresent()) {
            log.info("Tag '{}' already exists", tagName);
            return false;
        }
        Tag newTag = create(tagName);
        log.info("Added new tag '{}', group '{}'", newTag.getName(), newTag.getTagGroup());
        return true;
    }

    public Tag create(String tagName) {
        TagGroup tagGroup = TagGroup.COMMON;
        if (tagName.startsWith("Блог компании ")) {
            tagGroup = TagGroup.BLOG;
        }
        return tagRepository.save(new Tag(tagName, tagGroup));
    }

    public Optional<Tag> getById(UUID uuid) {
        return tagRepository.findById(uuid);
    }

    private Optional<Tag> getByTagName(String tag) {
        return tagRepository.findByName(tag);
    }

    public Page<Tag> getAll(int page, int size) {
        return tagRepository
                .findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
    }

    public Page<Tag> getAllByTagGroup(TagGroup tagGroup, int page, int size) {
        return tagRepository.findAllByTagGroupIs(
                tagGroup,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"))
        );
    }

    public Page<Tag> getWithoutUserTags(UUID userId, TagGroup tagGroup, int page, int size) {
        Page<Tag> tagPage = tagRepository.findWithoutUserTagsByUserIdAndTagGroup(
                userId, tagGroup.name(), PageRequest.of(page, size));
        if (page > tagPage.getTotalPages() - 1) {
            return tagRepository.findWithoutUserTagsByUserIdAndTagGroup(
                    userId, tagGroup.name(), PageRequest.of(--page, size));
        }
        return tagPage;
    }

    public Page<Tag> getAllCommon(int page, int size) {
        return getAllByTagGroup(TagGroup.COMMON, page, size);
    }

    public Page<Tag> getAllBlog(int page, int size) {
        return getAllByTagGroup(TagGroup.BLOG, page, size);
    }
}
