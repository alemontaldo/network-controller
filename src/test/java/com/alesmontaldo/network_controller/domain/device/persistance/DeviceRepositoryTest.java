package com.alesmontaldo.network_controller.domain.device.persistance;

import com.alesmontaldo.network_controller.codegen.types.AccessPoint;
import com.alesmontaldo.network_controller.codegen.types.Device;
import com.alesmontaldo.network_controller.codegen.types.Gateway;
import com.alesmontaldo.network_controller.codegen.types.Switch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class DeviceRepositoryTest {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceMongoRepository deviceMongoRepository;

    private GatewayDocument testGateway;

    @BeforeEach
    void setUp() {
        // Clean the database before each test
        deviceMongoRepository.deleteAll();

        // Create and save a test device
        testGateway = new GatewayDocument();
        testGateway.setMac("AA:BB:CC:DD:EE:FF");
        testGateway.setUplinkMac(null);
        testGateway.setDeviceType("GATEWAY");
        
        testGateway = deviceMongoRepository.save(testGateway);
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        deviceMongoRepository.deleteAll();
    }

    @Test
    void getDeviceById_shouldReturnDevice_whenDeviceExists() {
        // Act
        Optional<Device> result = deviceRepository.findById(testGateway.getMac());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(Gateway.class);
        Gateway gateway = (Gateway) result.get();
        assertThat(gateway.getMac()).isEqualTo("AA:BB:CC:DD:EE:FF");
    }

    @Test
    void getDeviceById_shouldReturnEmpty_whenDeviceDoesNotExist() {
        // Act
        Optional<Device> result = deviceRepository.findById("non-existent-mac");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getDeviceById_shouldReturnCorrectDeviceType() {
        // Arrange
        SwitchDocument switchDevice = new SwitchDocument();
        switchDevice.setMac("11:22:33:44:55:66");
        switchDevice.setUplinkMac(testGateway.getMac());
        switchDevice.setDeviceType("SWITCH");
        switchDevice = deviceMongoRepository.save(switchDevice);

        AccessPointDocument accessPoint = new AccessPointDocument();
        accessPoint.setMac("AA:BB:CC:11:22:33");
        accessPoint.setUplinkMac(switchDevice.getMac());
        accessPoint.setDeviceType("ACCESS_POINT");
        accessPoint = deviceMongoRepository.save(accessPoint);

        // Act
        Optional<Device> gatewayResult = deviceRepository.findById(testGateway.getMac());
        Optional<Device> switchResult = deviceRepository.findById(switchDevice.getMac());
        Optional<Device> apResult = deviceRepository.findById(accessPoint.getMac());

        // Assert
        assertThat(gatewayResult).isPresent();
        assertThat(gatewayResult.get()).isInstanceOf(Gateway.class);
        
        assertThat(switchResult).isPresent();
        assertThat(switchResult.get()).isInstanceOf(Switch.class);
        
        assertThat(apResult).isPresent();
        assertThat(apResult.get()).isInstanceOf(AccessPoint.class);
    }
}
