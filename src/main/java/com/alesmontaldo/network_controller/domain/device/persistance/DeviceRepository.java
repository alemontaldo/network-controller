package com.alesmontaldo.network_controller.domain.device.persistance;

import com.alesmontaldo.network_controller.codegen.types.Device;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import com.alesmontaldo.network_controller.infrastructure.lock.DistributedLockService;
import jakarta.validation.ValidationException;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GraphLookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

@Repository
public class DeviceRepository {

    private static final Log log = LogFactory.getLog(DeviceRepository.class);

    private final DeviceMongoRepository deviceMongoRepository;
    private final DeviceMapper deviceMapper;
    private final MongoTemplate mongoTemplate;
    private final DistributedLockService lockService;

    @Autowired
    public DeviceRepository(DeviceMongoRepository deviceMongoRepository,
                            DeviceMapper deviceMapper,
                            MongoTemplate mongoTemplate,
                            DistributedLockService lockService) {
        this.deviceMongoRepository = deviceMongoRepository;
        this.deviceMapper = deviceMapper;
        this.mongoTemplate = mongoTemplate;
        this.lockService = lockService;
    }

    public Optional<Device> findById(MacAddress id) {
        Optional<DeviceDocument> deviceDocument = deviceMongoRepository.findById(id);
        return deviceDocument.map(deviceMapper::toDevice);
    }

    /**
     * Saves a device with distributed locking to ensure topology consistency.
     * Validates that adding the device won't create cycles in the network topology.
     *
     * @param device The device to save
     * @return The saved device
     * @throws ConcurrentModificationException if unable to acquire a lock
     * @throws ValidationException if adding the device would create a cycle
     */
    public Device save(Device device) {
        String lockToken = lockService.acquireLock();
        if (lockToken == null) {
            throw new ConcurrentModificationException("Network topology is currently being modified. Please try again later.");
        }
        
        try {
            validateEventualNewCycle(device);

            log.info("Adding new device: " + device);
            DeviceDocument deviceDocument = deviceMongoRepository.save(deviceMapper.toDocument(device));
            return deviceMapper.toDevice(deviceDocument);
        } finally {
            lockService.releaseLock(lockToken);
        }
    }

    private void validateEventualNewCycle(Device device) {
        MacAddress mac = device.getMac();
        MacAddress uplinkMac = device.getUplinkMac();

        if (uplinkMac != null) {
            Optional<Device> uplinkDevice = findById(uplinkMac);
            if (uplinkDevice.isEmpty()) {
                throw new ValidationException("Uplink device with MAC: " + uplinkMac + " does not exist");
            }

            if (wouldCreateCycle(mac, uplinkMac)) {
                throw new ValidationException("Adding this device would create a circular connection in the network topology");
            }
        }
    }

    /**
     * Checks if adding a device with the given MAC and uplink MAC would create a cycle in the topology.
     *
     * @param newDeviceMac The MAC of the device being added
     * @param directUplinkMac The uplink MAC of the device being added
     * @return true if adding this device would create a cycle, false otherwise
     */
    private boolean wouldCreateCycle(MacAddress newDeviceMac, MacAddress directUplinkMac) {
        // Start with the direct uplink
        MacAddress currentMac = directUplinkMac;

        // Set to keep track of visited devices to detect cycles
        Set<MacAddress> visitedMacs = new HashSet<>();

        // Traverse up the topology
        while (currentMac != null) {
            // If we've seen this MAC before, or if it's the same as the new device's MAC, we have a cycle
            if (!visitedMacs.add(currentMac) || currentMac.equals(newDeviceMac)) {
                return true;
            }

            // Get the current device's uplink
            Optional<Device> currentDevice = findById(currentMac);
            if (currentDevice.isEmpty()) {
                // If the device doesn't exist, we can't go further up
                break;
            }

            // Move to the uplink device
            currentMac = currentDevice.get().getUplinkMac();
        }

        return false;
    }

    /**
     * Fetches a single device document + its entire subtree (all descendants)
     * using $graphLookup. Returns a DeviceDocument whose `subtree` list
     * contains the root and all descendants (flat).
     *
     * @param rootMac the MAC address of the root node you want
     */
    public Optional<Device> fetchSubtree(MacAddress rootMac) {
        // 1) Match stage: find exactly the document whose mac == rootMac
        MatchOperation match = Aggregation.match(Criteria.where("_id").is(rootMac));

        // 2) $graphLookup stage: from "devices" collection,
        //    startWith "$mac" (the matched root's MAC),
        //    connectFromField = "mac", connectToField = "uplinkMac",
        //    as = "subtree"
        GraphLookupOperation graphLookup = GraphLookupOperation.builder()
                .from("devices")
                .startWith("_id")
                .connectFrom("_id")
                .connectTo("uplinkMac")
                .as("downlinkDevices");

        // 3) Build the aggregation pipeline
        Aggregation agg = Aggregation.newAggregation(match, graphLookup);

        // 4) Execute the aggregation; map results directly into DeviceDocument.class
        AggregationResults<DeviceDocument> results =
                mongoTemplate.aggregate(agg, "devices", DeviceDocument.class);

        // There will be at most one document, since we matched on a unique MAC.
        if (results.getMappedResults().isEmpty()) {
            return Optional.empty();
        } else {
            DeviceDocument rootDevice = results.getMappedResults().getFirst();
            
            // Process the device tree to build the full hierarchy
            buildDeviceHierarchy(rootDevice);
            
            return Optional.of(deviceMapper.toDevice(rootDevice));
        }
    }
    
    /**
     * Recursively builds the device hierarchy by organizing the flat list of downlink devices
     * into a proper tree structure.
     */
    private void buildDeviceHierarchy(DeviceDocument device) {
        if (device.getDownlinkDevices() == null || device.getDownlinkDevices().isEmpty()) {
            // No children, set empty list
            device.setDownlinkDevices(List.of());
            return;
        }
        
        // Create a copy of the flat list of all descendants
        List<DeviceDocument> allDescendants = new ArrayList<>(device.getDownlinkDevices());
        
        // Get all direct children of this device
        List<DeviceDocument> directChildren = allDescendants.stream()
                .filter(child -> child.getUplinkMac() != null && 
                       child.getUplinkMac().equals(device.getMac()))
                .collect(Collectors.toList());
        
        // Set the direct children as the downlink devices
        device.setDownlinkDevices(directChildren);
        
        // Recursively process each child, passing the complete list of descendants
        for (DeviceDocument child : directChildren) {
            // For each child, we need to build its own device hierarchy

            // we remove the child itself from the copy of the flat list of all descendants
            List<DeviceDocument> childDescendants = allDescendants.stream()
                    .filter(descendant -> !descendant.getMac().equals(child.getMac()))
                    .collect(Collectors.toList());
            
            // Set the potential descendants to the child
            child.setDownlinkDevices(childDescendants);
            
            // Recursively build the hierarchy for this child
            buildDeviceHierarchy(child);
        }
    }
}
