package com.alesmontaldo.network_controller.domain.device.persistance.mongo_db;

import com.alesmontaldo.network_controller.codegen.types.*;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import com.alesmontaldo.network_controller.domain.device.persistance.DeviceRepository;
import jakarta.validation.ValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class DeviceMongoRepositoryTest {

    private static final Log log = LogFactory.getLog(DeviceMongoRepositoryTest.class);

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MongoRepository mongoRepository;

    @Autowired
    private DeviceMapper deviceMapper;

    private GatewayDocument testGateway;
    private SwitchDocument testSwitch;
    private AccessPointDocument testAccessPoint;

    @BeforeEach
    void setUp() {
        // Clean the database before each test
        mongoRepository.deleteAll();

        // Create and save a test gateway
        testGateway = new GatewayDocument();
        testGateway.setMacAddress(new MacAddress("AA:BB:CC:DD:EE:FF"));
        testGateway.setUplinkMacAddress(null);
        testGateway.setDeviceType(DeviceType.GATEWAY);
        testGateway = mongoRepository.save(testGateway);

        // Create and save a test switch connected to the gateway
        testSwitch = new SwitchDocument();
        testSwitch.setMacAddress(new MacAddress("11:22:33:44:55:66"));
        testSwitch.setUplinkMacAddress(testGateway.getMacAddress());
        testSwitch.setDeviceType(DeviceType.SWITCH);
        testSwitch = mongoRepository.save(testSwitch);

        // Create and save a test access point connected to the switch
        testAccessPoint = new AccessPointDocument();
        testAccessPoint.setMacAddress(new MacAddress("AA:BB:CC:11:22:33"));
        testAccessPoint.setUplinkMacAddress(testSwitch.getMacAddress());
        testAccessPoint.setDeviceType(DeviceType.ACCESS_POINT);
        testAccessPoint = mongoRepository.save(testAccessPoint);
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        mongoRepository.deleteAll();
    }

    @Test
    void getDeviceById_shouldReturnDevice_whenDeviceExists() {
        log.info("running getDeviceById_shouldReturnDevice_whenDeviceExists test");

        // Act
        Optional<Device> result = deviceRepository.findById(testGateway.getMacAddress());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(Gateway.class);
        Gateway gateway = (Gateway) result.get();
        assertThat(gateway.getMacAddress()).isEqualTo(new MacAddress("AA:BB:CC:DD:EE:FF"));
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
        // Act
        Optional<Device> gatewayResult = deviceRepository.findById(testGateway.getMacAddress());
        Optional<Device> switchResult = deviceRepository.findById(testSwitch.getMacAddress());
        Optional<Device> apResult = deviceRepository.findById(testAccessPoint.getMacAddress());

        // Assert
        assertThat(gatewayResult).isPresent();
        assertThat(gatewayResult.get()).isInstanceOf(Gateway.class);
        
        assertThat(switchResult).isPresent();
        assertThat(switchResult.get()).isInstanceOf(Switch.class);
        
        assertThat(apResult).isPresent();
        assertThat(apResult.get()).isInstanceOf(AccessPoint.class);
    }

    @Test
    void findAll_shouldReturnAllDevices() {
        // Act
        List<Device> devices = deviceRepository.findAll();

        // Assert
        assertThat(devices).hasSize(3);
        assertThat(devices).extracting("macAddress")
                .contains(
                        testGateway.getMacAddress(),
                        testSwitch.getMacAddress(),
                        testAccessPoint.getMacAddress()
                );
    }

    @Test
    void save_shouldSaveDevice_whenDeviceIsValid() {
        // Arrange
        MacAddress newMac = new MacAddress("FF:FF:FF:FF:FF:FF");
        Gateway newGateway = new Gateway(newMac, null, DeviceType.GATEWAY, List.of());

        // Act
        Device savedDevice = deviceRepository.save(newGateway);

        // Assert
        assertThat(savedDevice).isNotNull();
        assertThat(savedDevice.getMacAddress()).isEqualTo(newMac);
        assertThat(savedDevice.getDeviceType()).isEqualTo(DeviceType.GATEWAY);

        // Verify it was actually saved to the database
        Optional<Device> retrievedDevice = deviceRepository.findById(newMac);
        assertThat(retrievedDevice).isPresent();
        assertThat(retrievedDevice.get().getMacAddress()).isEqualTo(newMac);
    }

    @Test
    void save_shouldThrowException_whenUplinkDeviceDoesNotExist() {
        // Arrange
        MacAddress nonExistentMac = new MacAddress("99:99:99:99:99:99");
        MacAddress newMac = new MacAddress("FF:FF:FF:FF:FF:FF");
        Switch newSwitch = new Switch(newMac, nonExistentMac, DeviceType.SWITCH, List.of());

        // Act & Assert
        assertThatThrownBy(() -> deviceRepository.save(newSwitch))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    void save_shouldThrowException_whenCreatingCycle() {
        // Arrange - Create a cycle: Gateway -> Switch -> AccessPoint -> Gateway
        AccessPoint cyclicAp = new AccessPoint(
                new MacAddress("CC:CC:CC:CC:CC:CC"),
                testGateway.getMacAddress(),
                DeviceType.ACCESS_POINT,
                List.of()
        );
        mongoRepository.save(deviceMapper.toDocument(cyclicAp));

        // Try to update the gateway to point to the access point, creating a cycle
        Gateway cyclicGateway = new Gateway(
                testGateway.getMacAddress(),
                cyclicAp.getMacAddress(),
                DeviceType.GATEWAY,
                List.of()
        );

        // Act & Assert
        assertThatThrownBy(() -> deviceRepository.save(cyclicGateway))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("circular connection");
    }

    @Test
    void fetchSubtree_shouldReturnCompleteSubtree() {
        // Act
        Optional<Device> result = deviceRepository.fetchSubtree(testGateway.getMacAddress());

        // Assert
        assertThat(result).isPresent();
        Device rootDevice = result.get();
        assertThat(rootDevice.getMacAddress()).isEqualTo(testGateway.getMacAddress());
        
        // Check first level children
        List<Device> firstLevelChildren = rootDevice.getDownlinkDevices();
        assertThat(firstLevelChildren).hasSize(1);
        assertThat(firstLevelChildren.getFirst().getMacAddress()).isEqualTo(testSwitch.getMacAddress());
        
        // Check second level children
        List<Device> secondLevelChildren = firstLevelChildren.getFirst().getDownlinkDevices();
        assertThat(secondLevelChildren).hasSize(1);
        assertThat(secondLevelChildren.getFirst().getMacAddress()).isEqualTo(testAccessPoint.getMacAddress());
        
        // Check that leaf nodes have empty downlink lists
        assertThat(secondLevelChildren.getFirst().getDownlinkDevices()).isEmpty();
    }

    @Test
    void fetchSubtree_shouldReturnEmpty_whenDeviceDoesNotExist() {
        // Act
        Optional<Device> result = deviceRepository.fetchSubtree(new MacAddress("99:99:99:99:99:99"));

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void fetchSubtree_shouldReturnCorrectSubtree_whenStartingFromMiddleNode() {
        // Act
        Optional<Device> result = deviceRepository.fetchSubtree(testSwitch.getMacAddress());

        // Assert
        assertThat(result).isPresent();
        Device rootDevice = result.get();
        assertThat(rootDevice.getMacAddress()).isEqualTo(testSwitch.getMacAddress());
        
        // Check children
        List<Device> children = rootDevice.getDownlinkDevices();
        assertThat(children).hasSize(1);
        assertThat(children.getFirst().getMacAddress()).isEqualTo(testAccessPoint.getMacAddress());
        
        // Verify the parent (gateway) is not included in the subtree
        assertThat(rootDevice.getUplinkMacAddress()).isEqualTo(testGateway.getMacAddress());
        assertThat(children).extracting("macAddress").doesNotContain(testGateway.getMacAddress());
    }
}
