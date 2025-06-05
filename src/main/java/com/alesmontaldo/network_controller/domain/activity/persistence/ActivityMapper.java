package com.alesmontaldo.network_controller.domain.activity.persistence;

import com.alesmontaldo.network_controller.domain.activity.Activity;
import com.alesmontaldo.network_controller.domain.activity.Run;
import com.alesmontaldo.network_controller.domain.activity.Swim;
import com.alesmontaldo.network_controller.domain.activity.Rowing;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public abstract class ActivityMapper {

    private ActivityMongoRepository activityMongoRepository;

    @Autowired
    public void setActivityMongoMapper(ActivityMongoRepository activityMongoRepository) {
        this.activityMongoRepository = activityMongoRepository;
    }

    /**
     * Maps an ActivityDocument entity to an Activity domain object
     * This is a dispatcher method that will call the appropriate method based on the type
     */
    public Activity toActivity(ActivityDocument document) {
        if (document == null) {
            return null;
        }
        
        return switch (document.getType()) {
            case "RUN" -> toRun((RunDocument) document);
            case "SWIM" -> toSwim((SwimDocument) document);
            case "ROWING" -> toRowing((RowingDocument) document);
            default -> throw new IllegalArgumentException("Unknown activity type: " + document.getType());
        };
    }

    /**
     * Maps an Activity domain object to an ActivityDocument entity
     * This is a dispatcher method that will call the appropriate method based on the runtime type
     */
    public ActivityDocument toDocument(Activity activity) {
        return switch (activity) {
            case null -> null;
            case Run run -> toRunDocument(run);
            case Swim swim -> toSwimDocument(swim);
            case Rowing rowing -> toRowingDocument(rowing);
            default -> throw new IllegalArgumentException("Unknown activity type: " + activity.getClass().getName());
        };
    }
    
    // Specific mappers for each concrete type
    public abstract Run toRun(RunDocument document);

    public abstract Swim toSwim(SwimDocument document);

    public abstract Rowing toRowing(RowingDocument document);
    
    @Mapping(target = "type", ignore = true)
    public abstract RunDocument toRunDocument(Run run);
    
    @Mapping(target = "type", ignore = true)
    public abstract SwimDocument toSwimDocument(Swim swim);
    
    @Mapping(target = "type", ignore = true)
    public abstract RowingDocument toRowingDocument(Rowing rowing);
    
    @AfterMapping
    protected void setRunType(@MappingTarget RunDocument document) {
        document.setType("RUN");
    }
    
    @AfterMapping
    protected void setSwimType(@MappingTarget SwimDocument document) {
        document.setType("SWIM");
    }
    
    @AfterMapping
    protected void setRowingType(@MappingTarget RowingDocument document) {
        document.setType("ROWING");
    }
}
