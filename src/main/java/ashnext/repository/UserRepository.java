package ashnext.repository;

import ashnext.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.tags WHERE u.id=:id")
    Optional<User> findByIdWithTags(@Param("id") UUID id);

    User findByTelegramUserId(@Param("telegramUserId") Long telegramUserId);

    List<User> findAllByActiveTrueAndSubscriptionTrue();
}
