package com.alesmontaldo.network_controller.domain.device.persistance;


import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "devices")
public class AccessPointDocument extends DeviceDocument {

    public AccessPointDocument() {
        super();
        setDeviceType("ACCESS_POINT");
    }

    public AccessPointDocument(String mac,
                               String uplinkMac,
                               String deviceType) {
        super(mac, uplinkMac, deviceType);
        setDeviceType("ACCESS_POINT");
    }
}
