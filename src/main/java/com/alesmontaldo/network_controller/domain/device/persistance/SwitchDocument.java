package com.alesmontaldo.network_controller.domain.device.persistance;


import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "devices")
public class SwitchDocument extends DeviceDocument {

    public SwitchDocument() {
        super();
        setDeviceType("SWITCH");
    }

    public SwitchDocument(String mac,
                          String uplinkMac,
                          String deviceType) {
        super(mac, uplinkMac, deviceType);
        setDeviceType("SWITCH");
    }
}
