package com.alesmontaldo.network_controller.domain.activity.persistence;

import com.alesmontaldo.network_controller.domain.activity.Activity;
import com.alesmontaldo.network_controller.domain.comment.Comment;
import com.alesmontaldo.network_controller.domain.comment.persistence.CommentMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ActivityRepository {

    private final ActivityMongoRepository activityMongoRepository;
    private final ActivityMapper activityMapper;
    private final CommentMapper commentMapper;

    public ActivityRepository(ActivityMongoRepository activityMongoRepository,
                              ActivityMapper activityMapper,
                              CommentMapper commentMapper) {
        this.activityMongoRepository = activityMongoRepository;
        this.activityMapper = activityMapper;
        this.commentMapper = commentMapper;
    }

    public Optional<Activity> findById(String text) {
        return activityMongoRepository.findById(text).map(activityMapper::toActivity);
    }

    public List<Comment> findCommentsByActivityId(String activityId) {
        Optional<ActivityDocument> dbActivity = activityMongoRepository.findById(activityId);
        return dbActivity.map(activityDocument ->
                activityDocument.getComments().stream()
                        .map(commentMapper::toComment)
                        .toList()).orElseGet(List::of);
    }

    public Activity save(Activity activity) {
        ActivityDocument document = activityMapper.toDocument(activity);
        return activityMapper.toActivity(activityMongoRepository.save(document));
    }
}
