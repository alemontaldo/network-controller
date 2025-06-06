package com.alesmontaldo.network_controller.domain.device.persistance;

import com.alesmontaldo.network_controller.domain.device.MacAddress;
import org.springframework.data.mongodb.repository.MongoRepository;


interface DeviceMongoRepository extends MongoRepository<DeviceDocument, MacAddress> {
}
