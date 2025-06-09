package com.alesmontaldo.network_controller.infrastructure.lock;

import com.alesmontaldo.network_controller.domain.device.persistance.mongo_db.lock.TopologyLock;
import com.mongodb.client.result.DeleteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/**
 * Service for managing distributed locks in MongoDB.
 * Provides methods to acquire and release locks for topology operations.
 * Relies on MongoDB's TTL index for automatic expiration of locks.
 */
@Service
@Profile("!in-memory")
public class DistributedLockService {
    private final MongoTemplate mongoTemplate;
    private final int LOCK_TIMEOUT_SECONDS = 30;
    
    @Autowired
    public DistributedLockService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
    /**
     * Tries to acquire a lock for the topology operations
     * @return A unique lock token if successful, null if the lock couldn't be acquired
     */
    public String acquireLock() {
        String lockToken = UUID.randomUUID().toString();
        Date now = new Date();
        Date expiration = new Date(now.getTime() + (LOCK_TIMEOUT_SECONDS * 1000));
        
        TopologyLock newLock = new TopologyLock(lockToken, now, expiration);
        
        try {
            // Try to insert the lock document - will fail if it already exists
            // MongoDB's TTL index will automatically remove expired locks
            mongoTemplate.insert(newLock);
            return lockToken;
        } catch (DuplicateKeyException e) {
            // Lock already exists and is still valid
            return null;
        }
    }
    
    /**
     * Releases the lock if the caller is the owner
     * @param lockToken The token returned when the lock was acquired
     * @return true if the lock was released, false otherwise
     */
    public boolean releaseLock(String lockToken) {
        if (lockToken == null) {
            return false;
        }
        
        Query query = Query.query(
            Criteria.where("_id").is("TOPOLOGY_LOCK")
                   .and("owner").is(lockToken)
        );
        
        DeleteResult result = mongoTemplate.remove(query, TopologyLock.class);
        return result.getDeletedCount() > 0;
    }
}
