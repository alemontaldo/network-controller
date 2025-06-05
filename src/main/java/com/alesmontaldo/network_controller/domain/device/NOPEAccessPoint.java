package com.alesmontaldo.network_controller.domain.device;

import java.util.List;

public record NOPEAccessPoint(
        String mac,
        String uplinkMac,
        List<NOPEDevice> downlinkDevices) implements NOPEDevice {
}
