package ashnext.repository;

import ashnext.model.Tag;
import ashnext.model.TagGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, String> {

    Tag findByName(String tag);

    Page<Tag> findAllByTagGroupIs(TagGroup tagGroup, Pageable pageable);
}
