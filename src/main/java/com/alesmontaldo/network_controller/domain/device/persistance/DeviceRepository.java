package com.alesmontaldo.network_controller.domain.device.persistance;

import com.alesmontaldo.network_controller.codegen.types.Device;
import com.alesmontaldo.network_controller.domain.club.Club;
import com.alesmontaldo.network_controller.domain.club.persistence.ClubDocument;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import java.util.Optional;
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

    private final DeviceMongoRepository deviceMongoRepository;
    private final DeviceMapper deviceMapper;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public DeviceRepository(DeviceMongoRepository deviceMongoRepository,
                            DeviceMapper deviceMapper,
                            MongoTemplate mongoTemplate) {
        this.deviceMongoRepository = deviceMongoRepository;
        this.deviceMapper = deviceMapper;
        this.mongoTemplate = mongoTemplate;
    }

    public Optional<Device> findById(MacAddress id) {
        Optional<DeviceDocument> deviceDocument = deviceMongoRepository.findById(id);
        return deviceDocument.map(deviceMapper::toDevice);
    }

    public Device save(Device club) {
        DeviceDocument clubDocument = deviceMongoRepository.save(deviceMapper.toDocument(club));
        return deviceMapper.toDevice(clubDocument);
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
        //    startWith “$mac” (the matched root’s MAC),
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
            return Optional.of(results.getMappedResults().getFirst()).map(deviceMapper::toDevice);
        }
    }

    //Extra feature
    public void deleteById(MacAddress id) {
        deviceMongoRepository.deleteById(id);
    }
}
