package ashnext.service;

import ashnext.model.Tag;
import ashnext.repository.TagRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            create(new Tag(tagName));
            return true;
        }
        log.info("Tag '{}' already exists", tagName);
        return false;
    }

    public Tag create(Tag tag) {
        log.info("Added new tag '{}'", tag.getName());
        return tagRepository.save(tag);
    }

    private Tag getByTagName(String tag) {
        return tagRepository.findByName(tag);
    }
}
