package com.alesmontaldo.network_controller.application;

import com.alesmontaldo.network_controller.domain.activity.persistence.ActivityMapper;
import com.alesmontaldo.network_controller.domain.athlete.Athlete;
import com.alesmontaldo.network_controller.domain.athlete.persistence.AthleteDocument;
import com.alesmontaldo.network_controller.domain.athlete.persistence.AthleteRepository;
import com.alesmontaldo.network_controller.domain.club.persistence.ClubRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    private static final Log log = LogFactory.getLog(SearchService.class);

    private final ClubRepository clubRepository;
    private final AthleteRepository athleteRepository;

    public SearchService(ClubRepository clubRepository,
                         AthleteRepository athleteMongoRepository) {
        this.clubRepository = clubRepository;
        this.athleteRepository = athleteMongoRepository;
    }

    public List<Object> searchByText(String text) {
        log.info("Searching for: " + text);

        // Find clubs
        List<Object> results = new ArrayList<>(clubRepository.findByNameContainingIgnoreCase(text));

        // Find athletes
        results.addAll(athleteRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(text));
        
        return results;
    }
}
