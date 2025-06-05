package com.alesmontaldo.network_controller.domain.activity.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

interface ActivityMongoRepository extends MongoRepository<ActivityDocument, String> {
    
    List<ActivityDocument> findByType(String type);
}
