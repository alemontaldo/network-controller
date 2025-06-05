package com.alesmontaldo.network_controller.domain.club.persistence;

import com.alesmontaldo.network_controller.domain.club.Club;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Base64;
import java.util.List;

@Repository
public class ClubRepository {

    private final ClubMongoRepository clubMongoRepository;
    private final ClubMapper clubMapper;

    public ClubRepository(ClubMongoRepository clubMongoRepository,
                          ClubMapper clubMapper) {
        this.clubMongoRepository = clubMongoRepository;
        this.clubMapper = clubMapper;
    }

    public List<Club> findByNameContainingIgnoreCase(String text) {
        return clubMongoRepository.findByNameContainingIgnoreCase(text).stream()
                .map(clubMapper::toClub)
                .toList();
    }

    public Club save(Club club) {
        ClubDocument clubDocument = clubMongoRepository.save(clubMapper.toDocument(club));
        return clubMapper.toClub(clubDocument);
    }

    public List<Club> saveAll(List<Club> clubs) {
        List<ClubDocument> clubDocuments = clubMongoRepository.saveAll(clubs.stream()
                .map(clubMapper::toDocument)
                .toList());

        return clubDocuments.stream()
                .map(clubMapper::toClub)
                .toList();
    }
    
    public List<Club> findAllWithPagination(int limit, String afterCursor) {
        String afterId = null;
        if (afterCursor != null && !afterCursor.isEmpty()) {
            afterId = decodeCursor(afterCursor);
        }
        
        List<ClubDocument> documents;
        if (afterId != null) {
            documents = clubMongoRepository.findByIdGreaterThan(afterId, PageRequest.of(0, limit, Sort.by("id")));
        } else {
            documents = clubMongoRepository.findAll(PageRequest.of(0, limit, Sort.by("id"))).getContent();
        }
        
        return documents.stream()
                .map(clubMapper::toClub)
                .toList();
    }
    
    public long countAll() {
        return clubMongoRepository.count();
    }
    
    public static String encodeCursor(String id) {
        return Base64.getEncoder().encodeToString(id.getBytes());
    }
    
    public static String decodeCursor(String cursor) {
        return new String(Base64.getDecoder().decode(cursor));
    }
}
