package com.alesmontaldo.network_controller.domain.athlete;

import com.alesmontaldo.network_controller.domain.activity.Activity;
import java.util.List;

public record Athlete(
        String id,
        String firstName,
        String lastName,
        List<Activity> activities
) {
}
