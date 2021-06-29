package ashnext.repository;

import ashnext.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    User findByTelegramUserId(Long telegramUserId);
}
