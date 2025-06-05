package com.alesmontaldo.network_controller.domain.device.persistance;


import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "devices")
public class GatewayDocument extends DeviceDocument {

    public GatewayDocument() {
        super();
        setDeviceType("GATEWAY");
    }

    public GatewayDocument(String mac,
                           String uplinkMac,
                           String deviceType) {
        super(mac, uplinkMac, deviceType);
        setDeviceType("GATEWAY");
    }
}
