package com.alesmontaldo.network_controller.application;

import com.alesmontaldo.network_controller.domain.athlete.Athlete;
import com.alesmontaldo.network_controller.domain.activity.Activity;
import com.alesmontaldo.network_controller.domain.athlete.persistence.AthleteRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AthleteService {

    private static final Log logger = LogFactory.getLog(AthleteService.class);

    private final AthleteRepository athleteRepository;

    public AthleteService(AthleteRepository athleteRepository) {
        this.athleteRepository = athleteRepository;
    }

    public Athlete getAthleteById(String id) {
        logger.info("Fetching athlete with ID: " + id);
        return athleteRepository.findById(id).orElse(null);
    }

    public List<Activity> getActivitiesForAthlete(Athlete athlete) {
        logger.info("Fetching activities for athlete: " + athlete);
        Optional<Athlete> athleteDoc = athleteRepository.findById(athlete.id());
        if (athleteDoc.isPresent()) {
            return athleteDoc.get().activities();
        }
        return new ArrayList<>();
    }
}
