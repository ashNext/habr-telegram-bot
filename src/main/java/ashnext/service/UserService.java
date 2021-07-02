package ashnext.service;

import ashnext.model.User;
import ashnext.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User create(User user) {
        user.setActive(true);
        user.setSubscription(false);
        return userRepository.save(user);
    }

    public User getByTelegramUserId(Long telegramUserId) {
        return userRepository.findByTelegramUserId(telegramUserId);
    }

    public void setActive(User user, boolean active) {
        user.setActive(active);
        userRepository.save(user);
    }

    private void setSubscription(User user, boolean subscribe) {
        user.setSubscription(subscribe);
        userRepository.save(user);
    }

    public void subscribe(User user) {
        setSubscription(user, true);
    }

    public void unsubscribe(User user) {
        setSubscription(user, false);
    }

    public List<User> getAllActiveAndSubscribe() {
        return userRepository.findAllByActiveTrueAndSubscriptionTrue();
    }
}
