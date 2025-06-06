package com.alesmontaldo.network_controller.domain.device.persistance;

import com.alesmontaldo.network_controller.codegen.types.DeviceType;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "devices")
public abstract class DeviceDocument {

    @Id
    protected MacAddress mac;          // e.g. "AA:BB:CC:DD:EE:FF"
    protected MacAddress uplinkMac;    // parent’s MAC, null if root
    protected DeviceType deviceType;

    @Version
    private Long version;

    // This field is purely for receiving the aggregation result.
    // It will hold “all descendants” (including the root itself).
    protected List<DeviceDocument> downlinkDevices;

    public DeviceDocument() {
    }

    public DeviceDocument(MacAddress mac,
                          MacAddress uplinkMac,
                          DeviceType deviceType) {
        this.mac = mac;
        this.uplinkMac = uplinkMac;
        this.deviceType = deviceType;
    }

    public MacAddress getMac() {
        return mac;
    }

    public void setMac(MacAddress mac) {
        this.mac = mac;
    }

    public MacAddress getUplinkMac() {
        return uplinkMac;
    }

    public void setUplinkMac(MacAddress uplinkMac) {
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
