package ashnext.service;

import ashnext.model.ReadLater;
import ashnext.model.User;
import ashnext.repository.ReadLaterRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ReadLaterService {

    private final ReadLaterRepository readLaterRepository;

    public ReadLater create(ReadLater readLater) {
        return readLaterRepository.save(readLater);
    }

    public List<ReadLater> getAllByUser(User user) {
        return readLaterRepository.findAllByUser(user);
    }
}
