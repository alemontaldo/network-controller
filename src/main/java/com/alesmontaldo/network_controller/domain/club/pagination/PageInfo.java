package com.alesmontaldo.network_controller.domain.club.pagination;

public record PageInfo(
    boolean hasNextPage,
    String endCursor
) {
}
