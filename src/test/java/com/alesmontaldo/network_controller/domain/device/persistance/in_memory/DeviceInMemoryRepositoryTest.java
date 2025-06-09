package com.alesmontaldo.network_controller.domain.device.persistance.in_memory;

import com.alesmontaldo.network_controller.codegen.types.*;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import com.alesmontaldo.network_controller.domain.device.persistance.AbstractDeviceRepositoryTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles("in-memory")
public class DeviceInMemoryRepositoryTest extends AbstractDeviceRepositoryTest {

    private DeviceInMemoryRepository getInMemoryRepository() {
        return (DeviceInMemoryRepository) deviceRepository;
    }

    @BeforeEach
    void setUp() {
        getInMemoryRepository().clearAllDevices();
        setupTestData();
    }

    @AfterEach
    void tearDown() {
        getInMemoryRepository().clearAllDevices();
    }

    @Override
    protected void setupTestData() {
        DeviceInMemoryRepository repo = getInMemoryRepository();
        
        // Create a gateway
        Gateway gateway = new Gateway(gatewayMac, null, DeviceType.GATEWAY, List.of());
        repo.addDeviceForTesting(gateway);
        
        // Create a switch connected to the gateway
        Switch switchDevice = new Switch(switchMac, gatewayMac, DeviceType.SWITCH, List.of());
        repo.addDeviceForTesting(switchDevice);
        
        // Create an access point connected to the switch
        AccessPoint accessPoint = new AccessPoint(accessPointMac, switchMac, DeviceType.ACCESS_POINT, List.of());
        repo.addDeviceForTesting(accessPoint);
    }

    @Override
    protected void cleanupTestDevice(MacAddress mac) {
        getInMemoryRepository().removeDevice(mac);
    }
}
