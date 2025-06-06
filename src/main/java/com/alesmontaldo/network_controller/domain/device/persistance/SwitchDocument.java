package com.alesmontaldo.network_controller.domain.device.persistance;


import com.alesmontaldo.network_controller.codegen.types.DeviceType;
import com.alesmontaldo.network_controller.codegen.types.Switch;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "devices")
public class SwitchDocument extends DeviceDocument {

    public SwitchDocument() {
        super();
        setDeviceType(DeviceType.SWITCH);
    }

    public SwitchDocument(String mac,
                          String uplinkMac,
                          DeviceType deviceType) {
        super(mac, uplinkMac, deviceType);
        setDeviceType(DeviceType.SWITCH);
    }
}
