package com.alesmontaldo.network_controller.infrastructure.lock;

import com.alesmontaldo.network_controller.domain.lock.TopologyLock;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/**
 * Service for managing distributed locks in MongoDB.
 * Provides methods to acquire, release, and extend locks for topology operations.
 */
@Service
public class DistributedLockService {
    private final MongoTemplate mongoTemplate;
    private final int LOCK_TIMEOUT_SECONDS = 30; // Lock expires after 30 seconds
    
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
            mongoTemplate.insert(newLock);
            return lockToken;
        } catch (DuplicateKeyException e) {
            // Lock already exists, check if it's expired
            TopologyLock existingLock = mongoTemplate.findById("TOPOLOGY_LOCK", TopologyLock.class);
            
            if (existingLock != null && existingLock.getExpiresAt().before(now)) {
                // Lock exists but has expired, try to replace it
                Query query = Query.query(
                    Criteria.where("_id").is("TOPOLOGY_LOCK")
                           .and("expiresAt").lt(now)
                );
                
                Update update = Update.update("owner", lockToken)
                                     .set("acquiredAt", now)
                                     .set("expiresAt", expiration);
                
                UpdateResult result = mongoTemplate.updateFirst(query, update, TopologyLock.class);
                
                if (result.getModifiedCount() > 0) {
                    return lockToken; // Successfully acquired the expired lock
                }
            }
            
            return null; // Couldn't acquire the lock
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
    
    /**
     * Extends the lock expiration time
     * @param lockToken The token returned when the lock was acquired
     * @return true if the lock was extended, false otherwise
     */
    public boolean extendLock(String lockToken) {
        if (lockToken == null) {
            return false;
        }
        
        Date expiration = new Date(System.currentTimeMillis() + (LOCK_TIMEOUT_SECONDS * 1000));
        
        Query query = Query.query(
            Criteria.where("_id").is("TOPOLOGY_LOCK")
                   .and("owner").is(lockToken)
        );
        
        Update update = Update.update("expiresAt", expiration);
        
        UpdateResult result = mongoTemplate.updateFirst(query, update, TopologyLock.class);
        return result.getModifiedCount() > 0;
    }
}
