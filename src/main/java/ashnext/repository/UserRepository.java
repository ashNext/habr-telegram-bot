package ashnext.repository;

import ashnext.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    User findByTelegramUserId(Long telegramUserId);

    List<User> findAllByActiveTrueAndSubscriptionTrue();
}
