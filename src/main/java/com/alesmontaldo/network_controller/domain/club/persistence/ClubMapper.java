package com.alesmontaldo.network_controller.domain.club.persistence;

import com.alesmontaldo.network_controller.domain.club.Club;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface ClubMapper {
    
    /**
     * Maps a ClubDocument entity to a Club domain object
     * 
     * @param document the ClubDocument to map from
     * @return the mapped Club domain object
     */
    Club toClub(ClubDocument document);
    
    /**
     * Maps a Club domain object to a ClubDocument entity
     * 
     * @param club the Club domain object to map from
     * @return the mapped ClubDocument entity
     */
    ClubDocument toDocument(Club club);
}
