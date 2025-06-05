package com.alesmontaldo.network_controller.domain.club.pagination;

import java.util.List;

public record ClubConnection(
    List<ClubEdge> clubEdges,
    PageInfo pageInfo,
    int totalCount
) {
}
