package com.alesmontaldo.network_controller.application;

import com.alesmontaldo.network_controller.domain.club.Club;
import com.alesmontaldo.network_controller.domain.club.pagination.ClubConnection;
import com.alesmontaldo.network_controller.domain.club.pagination.ClubEdge;
import com.alesmontaldo.network_controller.domain.club.pagination.PageInfo;
import com.alesmontaldo.network_controller.domain.club.persistence.ClubRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClubService {

    private static final Log log = LogFactory.getLog(ClubService.class);
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final ClubRepository clubRepository;

    public ClubService(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    public Club createClub(String name, int totalMembers) {
        log.info("Creating new club: " + name + " with " + totalMembers + " members");
        Club club = new Club(null, name, totalMembers);
        return clubRepository.save(club);
    }
    
    public ClubConnection getClubsConnection(Integer first, String after) {
        int limit = first != null ? first : DEFAULT_PAGE_SIZE;
        
        // Get one more item than requested to determine if there are more pages
        List<Club> clubs = clubRepository.findAllWithPagination(limit + 1, after);
        
        boolean hasNextPage = clubs.size() > limit;
        // Remove the extra item if we fetched one more than requested
        if (hasNextPage) {
            clubs = clubs.subList(0, limit);
        }
        
        // Create clubEdges from clubs
        List<ClubEdge> edges = clubs.stream()
                .map(club -> new ClubEdge(
                        ClubRepository.encodeCursor(club.id()),
                        club
                ))
                .toList();
        
        // Create page info
        String endCursor = !edges.isEmpty() ? edges.getLast().cursor() : null;
        PageInfo pageInfo = new PageInfo(hasNextPage, endCursor);
        
        // Get total count
        long totalCount = clubRepository.countAll();
        
        return new ClubConnection(edges, pageInfo, (int) totalCount);
    }
}
