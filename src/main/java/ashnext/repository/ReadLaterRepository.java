package ashnext.repository;

import ashnext.model.ReadLater;
import ashnext.model.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadLaterRepository extends JpaRepository<ReadLater, UUID> {

    List<ReadLater> findAllByUser(User user);

    List<ReadLater> findAllByUserAndPostUrl(User user, String postUrl);
}
