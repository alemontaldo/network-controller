package com.alesmontaldo.network_controller.infrastructure.persistence;

import static com.alesmontaldo.network_controller.infrastructure.data_generator.FakerUtils.*;

import com.alesmontaldo.network_controller.domain.activity.Activity;
import com.alesmontaldo.network_controller.domain.activity.Rowing;
import com.alesmontaldo.network_controller.domain.activity.Run;
import com.alesmontaldo.network_controller.domain.activity.Swim;
import com.alesmontaldo.network_controller.domain.activity.persistence.*;
import com.alesmontaldo.network_controller.domain.athlete.Athlete;
import com.alesmontaldo.network_controller.domain.athlete.persistence.AthleteDocument;
import com.alesmontaldo.network_controller.domain.athlete.persistence.AthleteRepository;
import com.alesmontaldo.network_controller.domain.club.Club;
import com.alesmontaldo.network_controller.domain.club.persistence.ClubDocument;
import com.alesmontaldo.network_controller.domain.club.persistence.ClubRepository;
import com.alesmontaldo.network_controller.domain.comment.Comment;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Configuration
public class DataInitializer {

    private final AthleteRepository athleteRepository;
    private final ActivityRepository activityRepository;
    private final ClubRepository clubRepository;

    public DataInitializer(AthleteRepository athleteRepository,
                           ActivityRepository activityRepository,
                           ClubRepository clubRepository) {
        this.athleteRepository = athleteRepository;
        this.activityRepository = activityRepository;
        this.clubRepository = clubRepository;
    }

    @Bean
    public CommandLineRunner initData(MongoTemplate mongoTemplate) {
        return args -> {
            // Clear existing data
            mongoTemplate.dropCollection(ClubDocument.class);
            mongoTemplate.dropCollection(AthleteDocument.class);
            mongoTemplate.dropCollection(ActivityDocument.class);
            
            // Create clubs
            List<Club> clubs = createClubs();
            clubRepository.saveAll(clubs);
            
            // Create athletes with activities and comments
            List<Athlete> athletes = createAthletes();
            
            for (Athlete athlete : athletes) {
                // Save athlete with references to activities
                athleteRepository.save(athlete);
            }
            
            System.out.println("MongoDB initialized with sample data!");
        };
    }
    
    private List<Club> createClubs() {
        List<Club> clubs = new ArrayList<>();
        
        clubs.add(new Club(null, "Riverside Runners", randomInt(50, 500)));
        clubs.add(new Club(null, "Newnham Riverbank Club", randomInt(50, 500)));
        clubs.add(new Club(null, "London City Runners", randomInt(50, 500)));
        clubs.add(new Club(null, "Jesus Green Lido", randomInt(50, 500)));
        clubs.add(new Club(null, "City of Cambridge Rowing Club", randomInt(50, 500)));
        clubs.add(new Club(null, "Oxford Academicals Rowing Club", randomInt(50, 500)));
        clubs.add(new Club(null, "Parkside Pool and Gym", randomInt(50, 500)));
        clubs.add(new Club(null, "The Running Club London", randomInt(50, 500)));
        clubs.add(new Club(null, "Cambridge University Boat Club", randomInt(50, 500)));
        clubs.add(new Club(null, "Cambridge Harriers Athletics Club", randomInt(50, 500)));
        clubs.add(new Club(null, "City of Oxford Swimming Club", randomInt(50, 500)));
        clubs.add(new Club(null, "East End Road Runners", randomInt(50, 500)));
        clubs.add(new Club(null, "City of Oxford Rowing Club", randomInt(50, 500)));
        clubs.add(new Club(null, "Dulwich Park Runners", randomInt(50, 500)));
        
        return clubs;
    }
    
    private List<Athlete> createAthletes() {
        List<Athlete> athletes = new ArrayList<>();
        
        athletes.add(new Athlete(null, "Nestor", "Holt", createActivitiesForAthlete()));
        athletes.add(new Athlete(null, "Jose", "Graham", createActivitiesForAthlete()));
        athletes.add(new Athlete(null, "Carmen", "Jenkins", createActivitiesForAthlete()));
        athletes.add(new Athlete(null, "Josh", "Hamill", createActivitiesForAthlete()));
        athletes.add(new Athlete(null, "Miguel", "Harris", createActivitiesForAthlete()));
        athletes.add(new Athlete(null, "June", "Holt", createActivitiesForAthlete()));
        athletes.add(new Athlete(null, "Tomas", "Ware", createActivitiesForAthlete()));
        athletes.add(new Athlete(null, "Wally", "Cohen", createActivitiesForAthlete()));
        athletes.add(new Athlete(null, "Diana", "Ray", createActivitiesForAthlete()));
        athletes.add(new Athlete(null, "Bryan", "Duarte", createActivitiesForAthlete()));
        athletes.add(new Athlete(null, "Jamey", "Blackwell", createActivitiesForAthlete()));
        athletes.add(new Athlete(null, "Heidi", "Hammond", createActivitiesForAthlete()));
        athletes.add(new Athlete(null, "Kelley", "Bowman", createActivitiesForAthlete()));
        athletes.add(new Athlete(null, "Don", "Montes", createActivitiesForAthlete()));
        athletes.add(new Athlete(null, "Jordan", "Page", createActivitiesForAthlete()));
        athletes.add(new Athlete(null, "Paula", "Conrad", createActivitiesForAthlete()));
        athletes.add(new Athlete(null, "Joshua", "Moss", createActivitiesForAthlete()));
        athletes.add(new Athlete(null, "Jessica", "Stark", createActivitiesForAthlete()));
        athletes.add(new Athlete(null, "Andrea", "Barnes", createActivitiesForAthlete()));
        athletes.add(new Athlete(null, "Tom", "Hansen", createActivitiesForAthlete()));
        
        return athletes;
    }
    
    private List<Activity> createActivitiesForAthlete() {
        List<Activity> activities = new ArrayList<>();
        
        for (int i = 0; i < randomInt(1, 4); i++) {
            switch (randomInt(0, 2)) {
                case 0:
                    activities.add(activityRepository.save(new Run(null, generateDescription("run"), randomInt(0, 300), createComments())));
                    break;
                case 1:
                    activities.add(activityRepository.save(new Swim(null, generateDescription("swim"), randomInt(0, 160), randomBoolean(), createComments())));
                    break;
                case 2:
                    activities.add(activityRepository.save(new Rowing(null, generateDescription("rowing"), randomInt(90, 180), createComments())));
                    break;
            }
        }
        
        return activities;
    }
    
    private String generateDescription(String activityType) {
        return getRandomWeather() + ", " + getRandomMood() + " " + activityType;
    }
    
    private List<Comment> createComments() {
        Set<Comment> commentSet = new HashSet<>();
        int targetSize = randomInt(1, 3);
        
        while (commentSet.size() < targetSize) {
            commentSet.add(new Comment(getRandomQuote()));
        }
        
        return new ArrayList<>(commentSet);
    }
    
    // Random utility methods to replace FakerUtils
    
    private int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
