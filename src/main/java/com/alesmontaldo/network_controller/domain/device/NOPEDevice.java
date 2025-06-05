package com.alesmontaldo.network_controller.domain.device;

import java.util.List;

public interface NOPEDevice {
    String mac();
    String uplinkMac();
    List<NOPEDevice> downlinkDevices();
}
