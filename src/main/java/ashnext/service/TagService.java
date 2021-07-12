package ashnext.service;

import ashnext.model.Tag;
import ashnext.model.TagGroup;
import ashnext.repository.TagRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class TagService {

    private final TagRepository tagRepository;

    public List<Tag> getAll() {
        return tagRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public boolean addIfAbsent(String tagName) {
        if (getByTagName(tagName) == null) {
            create(tagName);
            return true;
        }
        log.info("Tag '{}' already exists", tagName);
        return false;
    }

    public Tag create(String tagName) {
        TagGroup tagGroup = TagGroup.COMMON;
        if (tagName.startsWith("Блог компании ")) {
            tagGroup = TagGroup.BLOG;
        }
        log.info("Add new tag '{}', group '{}'", tagName, tagGroup);
        return tagRepository.save(new Tag(tagName, tagGroup));
    }

    private Tag getByTagName(String tag) {
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

    public Page<Tag> getAllCommon(int page, int size) {
        return getAllByTagGroup(TagGroup.COMMON, page, size);
    }

    public Page<Tag> getAllBlog(int page, int size) {
        return getAllByTagGroup(TagGroup.BLOG, page, size);
    }
}
