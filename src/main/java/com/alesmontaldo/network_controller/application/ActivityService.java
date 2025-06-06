package com.alesmontaldo.network_controller.application;

import com.alesmontaldo.network_controller.domain.activity.Activity;
import com.alesmontaldo.network_controller.domain.activity.persistence.ActivityRepository;
import com.alesmontaldo.network_controller.domain.comment.Comment;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

@Service
public class ActivityService {
    private static final Log log = LogFactory.getLog(ActivityService.class);

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public Map<Activity, List<Comment>> getCommentsForActivities(List<Activity> activities) {
        log.info("Fetching comments for activities: " + activities);
        return activities.stream().collect(Collectors.toMap(
                Function.identity(),
                activity -> activityRepository.findCommentsByActivityId(activity.id())
        ));
    }
}
