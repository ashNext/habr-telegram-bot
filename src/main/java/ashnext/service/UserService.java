package ashnext.service;

import ashnext.model.User;
import ashnext.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User create(User user) {
        return userRepository.save(user);
    }

    public User getByTelegramUserId(Long telegramUserId){
        return userRepository.findByTelegramUserId(telegramUserId);
    }

    public void setActive(User user, boolean active){
        user.setActive(active);
        userRepository.save(user);
    }
}
