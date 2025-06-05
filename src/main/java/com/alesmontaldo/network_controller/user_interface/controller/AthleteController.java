package com.alesmontaldo.network_controller.user_interface.controller;

import com.alesmontaldo.network_controller.application.ActivityService;
import com.alesmontaldo.network_controller.domain.activity.*;
import com.alesmontaldo.network_controller.domain.athlete.Athlete;
import com.alesmontaldo.network_controller.application.AthleteService;
import com.alesmontaldo.network_controller.domain.comment.Comment;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class AthleteController {

    private final AthleteService athleteService;
    private final ActivityService activityService;

    public AthleteController(AthleteService athleteService,
                             ActivityService activityService) {
        this.athleteService = athleteService;
        this.activityService = activityService;
    }

    @QueryMapping
    public Athlete athlete(@Argument String id) {
        return athleteService.getAthleteById(id);
    }

    @SchemaMapping
    public List<Activity> activities(Athlete athlete) {
        return athleteService.getActivitiesForAthlete(athlete);
    }

    @BatchMapping
    public Map<Activity, List<Comment>> comments(List<Activity> activities) {
        return activityService.getCommentsForActivities(activities);
    }
}
