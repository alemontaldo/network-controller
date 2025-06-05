package com.alesmontaldo.network_controller.domain.club.pagination;

import com.alesmontaldo.network_controller.domain.club.Club;

public record ClubEdge(
    String cursor,
    Club club
) {
}
