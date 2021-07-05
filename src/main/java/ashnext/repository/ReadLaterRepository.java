package ashnext.repository;

import ashnext.model.ReadLater;
import ashnext.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReadLaterRepository extends JpaRepository<ReadLater, UUID> {

    List<ReadLater> findAllByUser(User user);
}
