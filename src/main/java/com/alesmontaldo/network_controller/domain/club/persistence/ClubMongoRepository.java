package com.alesmontaldo.network_controller.domain.club.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

interface ClubMongoRepository extends MongoRepository<ClubDocument, String> {
    
    @Query("{'name': {$regex: ?0, $options: 'i'}}")
    List<ClubDocument> findByNameContainingIgnoreCase(String text);
    
    List<ClubDocument> findByIdGreaterThan(String id, Pageable pageable);
}
