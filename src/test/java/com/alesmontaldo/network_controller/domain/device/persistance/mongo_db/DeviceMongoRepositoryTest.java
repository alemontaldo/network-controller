package com.alesmontaldo.network_controller.domain.device.persistance.mongo_db;

import com.alesmontaldo.network_controller.codegen.types.*;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import com.alesmontaldo.network_controller.domain.device.persistance.DeviceRepository;
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
public class DeviceMongoRepositoryTest {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MongoRepository mongoRepository;

    private GatewayDocument testGateway;

    @BeforeEach
    void setUp() {
        // Clean the database before each test
        mongoRepository.deleteAll();

        // Create and save a test device
        testGateway = new GatewayDocument();
        testGateway.setMac(new MacAddress("AA:BB:CC:DD:EE:FF"));
        testGateway.setUplinkMac(null);
        testGateway.setDeviceType(DeviceType.GATEWAY);
        
        testGateway = mongoRepository.save(testGateway);
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        mongoRepository.deleteAll();
    }

    @Test
    void getDeviceById_shouldReturnDevice_whenDeviceExists() {
        // Act
        Optional<Device> result = deviceRepository.findById(testGateway.getMac());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(Gateway.class);
        Gateway gateway = (Gateway) result.get();
        assertThat(gateway.getMac()).isEqualTo(new MacAddress("AA:BB:CC:DD:EE:FF"));
    }

    @Test
    void getDeviceById_shouldReturnEmpty_whenDeviceDoesNotExist() {
        // Act
        Optional<Device> result = deviceRepository.findById(new MacAddress("11:BB:CC:DD:EE:FF")); //non existent mac

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getDeviceById_shouldReturnCorrectDeviceType() {
        // Arrange
        SwitchDocument switchDevice = new SwitchDocument();
        switchDevice.setMac(new MacAddress("11:22:33:44:55:66"));
        switchDevice.setUplinkMac(testGateway.getMac());
        switchDevice.setDeviceType(DeviceType.SWITCH);
        switchDevice = mongoRepository.save(switchDevice);

        AccessPointDocument accessPoint = new AccessPointDocument();
        accessPoint.setMac(new MacAddress("AA:BB:CC:11:22:33"));
        accessPoint.setUplinkMac(switchDevice.getMac());
        accessPoint.setDeviceType(DeviceType.ACCESS_POINT);
        accessPoint = mongoRepository.save(accessPoint);

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
