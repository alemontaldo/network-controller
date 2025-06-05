package com.alesmontaldo.network_controller.domain.device.persistance;

import com.alesmontaldo.network_controller.domain.comment.persistence.CommentDocument;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "devices")
public abstract class DeviceDocument {

    @Id
    protected String mac;          // e.g. "AA:BB:CC:DD:EE:FF"
    protected String uplinkMac;    // parent’s MAC, null if root
    protected String deviceType;

    // This field is purely for receiving the aggregation result.
    // It will hold “all descendants” (including the root itself).
    protected List<DeviceDocument> downlinkDevices;

    public DeviceDocument() {
    }

    public DeviceDocument(String mac,
                          String uplinkMac,
                          String deviceType) {
        this.mac = mac;
        this.uplinkMac = uplinkMac;
        this.deviceType = deviceType;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getUplinkMac() {
        return uplinkMac;
    }

    public void setUplinkMac(String uplinkMac) {
        this.uplinkMac = uplinkMac;
    }

    public List<DeviceDocument> getDownlinkDevices() {
        return downlinkDevices;
    }

    public void setDownlinkDevices(List<DeviceDocument> downlinkDevices) {
        this.downlinkDevices = downlinkDevices;
    }
    
    public String getDeviceType() {
        return deviceType;
    }
    
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
