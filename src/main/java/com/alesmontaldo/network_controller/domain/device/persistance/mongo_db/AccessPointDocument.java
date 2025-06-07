package com.alesmontaldo.network_controller.domain.device.persistance.mongo_db;


import com.alesmontaldo.network_controller.codegen.types.DeviceType;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "devices")
public class AccessPointDocument extends DeviceDocument {

    public AccessPointDocument() {
        super();
        setDeviceType(DeviceType.ACCESS_POINT);
    }

    public AccessPointDocument(MacAddress mac,
                               MacAddress uplinkMac,
                               DeviceType deviceType) {
        super(mac, uplinkMac, deviceType);
        setDeviceType(DeviceType.ACCESS_POINT);
    }
}
