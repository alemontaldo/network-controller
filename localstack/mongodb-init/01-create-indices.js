// This script creates necessary indices for the network-controller application
// It will be executed when the MongoDB container is first initialized

// Switch to the database used for the app (or create it if it doesn't exist)
db = db.getSiblingDB('network-controller_db');

print("Creating TTL index for topology_locks collection...");

// Create TTL index for topology locks collection
// This will automatically remove documents when their expiresAt field is in the past
//  NOTE: The MongoDB TTL monitor runs approximately once every 60 seconds, so there can be up to a 60-second
// delay before expired documents are removed but for this assignment that's not an issue
db.topology_locks.createIndex(
    { "expiresAt": 1 },
    { 
        expireAfterSeconds: 0,
        background: true,
        name: "ttl_index_expiry"
    }
);

print("TTL index created successfully!");

// TODO: consider if to keep this
print("Creating indices for devices collection...");

// Index for faster lookups by uplinkMac
// The uplinkMac field represents the parent-child relationship between devices.
// We use uplinkMac when we query the network topology and
// when we look for cycles in the network at each tentative addition
db.devices.createIndex(
    { "uplinkMac": 1 },
    { 
        background: true,
        name: "idx_uplink_mac"
    }
);

print("All indices created successfully!");
