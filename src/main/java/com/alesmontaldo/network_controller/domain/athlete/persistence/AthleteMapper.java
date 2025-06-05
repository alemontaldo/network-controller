package com.alesmontaldo.network_controller.domain.athlete.persistence;

import com.alesmontaldo.network_controller.domain.activity.Activity;
import com.alesmontaldo.network_controller.domain.activity.persistence.ActivityDocument;
import com.alesmontaldo.network_controller.domain.activity.persistence.ActivityMapper;
import com.alesmontaldo.network_controller.domain.athlete.Athlete;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public abstract class AthleteMapper {

    private ActivityMapper activityMapper;

    @Autowired
    public void setActivityMongoMapper(ActivityMapper activityMapper) {
        this.activityMapper = activityMapper;
    }

    @Mapping(target = "activities", expression = "java(getActivities(document))")
    public abstract Athlete toAthlete(AthleteDocument document);

    @Mapping(target = "activities", expression = "java(getActivitiesDocuments(athlete))")
    public abstract AthleteDocument toDocument(Athlete athlete);

    protected List<Activity> getActivities(AthleteDocument document) {
        return document.getActivities().stream().map(activityMapper::toActivity).toList();
    }

    protected List<ActivityDocument> getActivitiesDocuments(Athlete athlete) {
        return athlete.activities().stream().map(activityMapper::toDocument).toList();
    }
}
