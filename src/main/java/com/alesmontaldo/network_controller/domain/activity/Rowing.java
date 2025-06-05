package com.alesmontaldo.network_controller.domain.activity;

import com.alesmontaldo.network_controller.domain.comment.Comment;
import java.util.List;

public record Rowing(
        String id,
        String description,
        int split,
        List<Comment> comments) implements Activity {
}
