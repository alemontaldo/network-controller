package com.alesmontaldo.network_controller.domain.device.persistance.mongo_db;


import com.alesmontaldo.network_controller.codegen.types.DeviceType;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "devices")
public class SwitchDocument extends DeviceDocument {

    public SwitchDocument() {
        super();
        setDeviceType(DeviceType.SWITCH);
    }

    public SwitchDocument(MacAddress macAddress,
                          MacAddress uplinkMacAddress,
                          DeviceType deviceType) {
        super(macAddress, uplinkMacAddress, deviceType);
        setDeviceType(DeviceType.SWITCH);
    }
}
