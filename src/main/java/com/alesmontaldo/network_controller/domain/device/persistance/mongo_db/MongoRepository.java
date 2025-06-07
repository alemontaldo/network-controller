package com.alesmontaldo.network_controller.domain.device.persistance.mongo_db;

import com.alesmontaldo.network_controller.domain.device.MacAddress;


interface MongoRepository extends org.springframework.data.mongodb.repository.MongoRepository<DeviceDocument, MacAddress> {
}
