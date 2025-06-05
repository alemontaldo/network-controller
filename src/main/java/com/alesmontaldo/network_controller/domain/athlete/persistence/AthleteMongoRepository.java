package com.alesmontaldo.network_controller.domain.athlete.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

interface AthleteMongoRepository extends MongoRepository<AthleteDocument, String> {
    
    @Query("{'$or': [{'firstName': {$regex: ?0, $options: 'i'}}, {'lastName': {$regex: ?0, $options: 'i'}}]}")
    List<AthleteDocument> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String text);
}
