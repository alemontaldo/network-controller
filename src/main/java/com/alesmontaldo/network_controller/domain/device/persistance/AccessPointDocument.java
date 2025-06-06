package com.alesmontaldo.network_controller.domain.device.persistance;


import com.alesmontaldo.network_controller.codegen.types.DeviceType;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "devices")
public class AccessPointDocument extends DeviceDocument {

    public AccessPointDocument() {
        super();
        setDeviceType(DeviceType.ACCESS_POINT);
    }

    public AccessPointDocument(String mac,
                               String uplinkMac,
                               DeviceType deviceType) {
        super(mac, uplinkMac, deviceType);
        setDeviceType(DeviceType.ACCESS_POINT);
    }
}
