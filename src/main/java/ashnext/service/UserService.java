package ashnext.service;

import ashnext.model.UserEntity;
import ashnext.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserEntity create(UserEntity userEntity) {
        return userRepository.save(userEntity);
    }
}
