package com.alesmontaldo.network_controller.domain.device.persistance.mongo_db;


import static com.alesmontaldo.network_controller.codegen.types.DeviceType.GATEWAY;

import com.alesmontaldo.network_controller.codegen.types.DeviceType;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "devices")
public class GatewayDocument extends DeviceDocument {

    public GatewayDocument() {
        super();
        setDeviceType(GATEWAY);
    }

    public GatewayDocument(MacAddress macAddress,
                           MacAddress uplinkMac,
                           DeviceType deviceType) {
        super(macAddress, uplinkMac, deviceType);
        setDeviceType(GATEWAY);
    }
}
