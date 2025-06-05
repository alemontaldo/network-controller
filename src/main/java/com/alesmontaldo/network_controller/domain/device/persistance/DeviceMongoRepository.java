package com.alesmontaldo.network_controller.domain.device.persistance;

import org.springframework.data.mongodb.repository.MongoRepository;


interface DeviceMongoRepository extends MongoRepository<DeviceDocument, String> {
}
