package com.alesmontaldo.network_controller.domain.athlete.persistence;

import com.alesmontaldo.network_controller.domain.athlete.Athlete;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AthleteRepository {

    private final AthleteMongoRepository athleteMongoRepository;
    private final AthleteMapper athleteMapper;

    public AthleteRepository(AthleteMongoRepository athleteMongoRepository,
                             AthleteMapper athleteMapper) {
        this.athleteMongoRepository = athleteMongoRepository;
        this.athleteMapper = athleteMapper;
    }

    public Optional<Athlete> findById(String id) {
        return athleteMongoRepository.findById(id).map(athleteMapper::toAthlete);
    }

    public Athlete save(Athlete athlete) {
        AthleteDocument document = athleteMapper.toDocument(athlete);
        return athleteMapper.toAthlete(athleteMongoRepository.save(document));
    }

    public List<Athlete> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String text) {
        return athleteMongoRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(text)
                .stream()
                .map(athleteMapper::toAthlete)
                .toList();
    }

}
