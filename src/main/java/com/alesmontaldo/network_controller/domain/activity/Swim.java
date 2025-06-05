package com.alesmontaldo.network_controller.domain.activity;

import com.alesmontaldo.network_controller.domain.comment.Comment;
import java.util.List;

public record Swim(
        String id,
        String description,
        int laps,
        boolean indoor,
        List<Comment> comments) implements Activity {
}
