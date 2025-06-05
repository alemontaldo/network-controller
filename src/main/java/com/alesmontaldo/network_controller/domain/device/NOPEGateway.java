package com.alesmontaldo.network_controller.domain.device;

import java.util.List;

public record NOPEGateway(
        String mac,
        String uplinkMac,
        List<NOPEDevice> downlinkDevices) implements NOPEDevice {
}
