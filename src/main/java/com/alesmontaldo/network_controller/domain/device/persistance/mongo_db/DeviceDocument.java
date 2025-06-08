package com.alesmontaldo.network_controller.domain.device.persistance.mongo_db;

import com.alesmontaldo.network_controller.codegen.types.DeviceType;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "devices")
public abstract class DeviceDocument {

    @Id
    protected MacAddress macAddress;
    protected MacAddress uplinkMac;
    protected DeviceType deviceType;

    // @Transactional operations are not needed in the scope of this
    // assignment, but IMHO it's better to include @Version since the beginning
    // for future-proofing
    @Version
    private Long version;

    // This field is purely for receiving the aggregation result.
    // It will hold “all descendants” (including the root itself).
    protected List<DeviceDocument> downlinkDevices;

    public DeviceDocument() {
    }

    public DeviceDocument(MacAddress macAddress,
                          MacAddress uplinkMacAddress,
                          DeviceType deviceType) {
        this.macAddress = macAddress;
        this.uplinkMac = uplinkMacAddress;
        this.deviceType = deviceType;
    }

    public MacAddress getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(MacAddress mac) {
        this.macAddress = mac;
    }

    public MacAddress getUplinkMacAddress() {
        return uplinkMac;
    }

    public void setUplinkMacAddress(MacAddress uplinkMac) {
        this.uplinkMac = uplinkMac;
    }

    public List<DeviceDocument> getDownlinkDevices() {
        return downlinkDevices;
    }

    public void setDownlinkDevices(List<DeviceDocument> downlinkDevices) {
        this.downlinkDevices = downlinkDevices;
    }
    
    public DeviceType getDeviceType() {
        return deviceType;
    }
    
    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }
}
