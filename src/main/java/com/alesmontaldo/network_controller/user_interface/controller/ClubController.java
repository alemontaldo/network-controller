package com.alesmontaldo.network_controller.user_interface.controller;

import com.alesmontaldo.network_controller.application.ClubService;
import com.alesmontaldo.network_controller.domain.club.Club;
import com.alesmontaldo.network_controller.domain.club.pagination.ClubConnection;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class ClubController {

    private final ClubService clubService;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    @MutationMapping
    public Club createClub(@Argument("input") Map<String, Object> input) {
        String name = (String) input.get("name");
        int totalMembers = (Integer) input.get("totalMembers");
        
        return clubService.createClub(name, totalMembers);
    }
    
    @QueryMapping
    public ClubConnection clubs(@Argument Integer first, @Argument String after) {
        return clubService.getClubsConnection(first, after);
    }
}
