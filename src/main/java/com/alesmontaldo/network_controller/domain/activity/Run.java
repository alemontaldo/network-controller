package com.alesmontaldo.network_controller.domain.activity;

import com.alesmontaldo.network_controller.domain.comment.Comment;
import java.util.List;

public record Run(
        String id,
        String description,
        int elevation,
        List<Comment> comments) implements Activity {
}
