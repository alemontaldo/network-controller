package com.alesmontaldo.network_controller.domain.device.persistance.mongo_db.lock;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Document representing a distributed lock for topology operations.
 * Used to ensure that only one thread/process can modify the network topology at a time.
 */
@Document(collection = "topology_locks")
public class TopologyLock {
    @Id
    private String id = "TOPOLOGY_LOCK"; // Single lock for the entire topology
    
    private String owner;        // Unique identifier for the thread/process holding the lock
    private Date acquiredAt;     // When the lock was acquired
    private Date expiresAt;      // Automatic expiration to prevent deadlocks
    
    public TopologyLock() {}
    
    public TopologyLock(String owner, Date acquiredAt, Date expiresAt) {
        this.owner = owner;
        this.acquiredAt = acquiredAt;
        this.expiresAt = expiresAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Date getAcquiredAt() {
        return acquiredAt;
    }

    public void setAcquiredAt(Date acquiredAt) {
        this.acquiredAt = acquiredAt;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }
}
