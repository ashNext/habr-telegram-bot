package ashnext.repository;

import ashnext.model.Tag;
import ashnext.model.TagGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    Optional<Tag> findByName(String name);

    Page<Tag> findAllByTagGroupIs(TagGroup tagGroup, Pageable pageable);
}
