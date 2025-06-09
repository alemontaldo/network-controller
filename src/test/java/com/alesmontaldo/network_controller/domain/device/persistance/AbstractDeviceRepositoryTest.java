package com.alesmontaldo.network_controller.domain.device.persistance;

import com.alesmontaldo.network_controller.codegen.types.*;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import jakarta.validation.ValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public abstract class AbstractDeviceRepositoryTest {

    private static final Log log = LogFactory.getLog(AbstractDeviceRepositoryTest.class);

    @Autowired
    protected DeviceRepository deviceRepository;

    protected MacAddress gatewayMac = new MacAddress("AA:BB:CC:DD:EE:FF");
    protected MacAddress switchMac = new MacAddress("11:22:33:44:55:66");
    protected MacAddress accessPointMac = new MacAddress("AA:BB:CC:11:22:33");

    protected abstract void setupTestData();
    protected abstract void cleanupTestDevice(MacAddress mac);

    @Test
    void getDeviceById_shouldReturnDevice_whenDeviceExists() {
        log.info("running getDeviceById_shouldReturnDevice_whenDeviceExists test");

        // Act
        Optional<Device> result = deviceRepository.findById(gatewayMac);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(Gateway.class);
        Gateway gateway = (Gateway) result.get();
        assertThat(gateway.getMacAddress()).isEqualTo(gatewayMac);
    }

    @Test
    void getDeviceById_shouldReturnEmpty_whenDeviceDoesNotExist() {
        // Act
        Optional<Device> result = deviceRepository.findById(new MacAddress("99:99:99:99:99:99")); // non-existent mac

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getDeviceById_shouldReturnCorrectDeviceType() {
        // Act
        Optional<Device> gatewayResult = deviceRepository.findById(gatewayMac);
        Optional<Device> switchResult = deviceRepository.findById(switchMac);
        Optional<Device> apResult = deviceRepository.findById(accessPointMac);

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
                        gatewayMac,
                        switchMac,
                        accessPointMac
                );
    }

    @Test
    void save_shouldSaveDevice_whenDeviceIsValid() {
        // Arrange
        MacAddress newMac = new MacAddress("FF:FF:FF:FF:FF:FF");
        Gateway newGateway = new Gateway(newMac, null, DeviceType.GATEWAY, List.of());

        try {
            // Act
            Device savedDevice = deviceRepository.save(newGateway);

            // Assert
            assertThat(savedDevice).isNotNull();
            assertThat(savedDevice.getMacAddress()).isEqualTo(newMac);
            assertThat(savedDevice.getDeviceType()).isEqualTo(DeviceType.GATEWAY);

            // Verify it was actually saved to the repository
            Optional<Device> retrievedDevice = deviceRepository.findById(newMac);
            assertThat(retrievedDevice).isPresent();
            assertThat(retrievedDevice.get().getMacAddress()).isEqualTo(newMac);
        } finally {
            // Clean up the test device
            cleanupTestDevice(newMac);
        }
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
        // Arrange - Create a device that would create a cycle if connected to the gateway
        MacAddress cyclicMac = new MacAddress("CC:CC:CC:CC:CC:CC");
        
        try {
            // First create a device that connects to the gateway
            AccessPoint cyclicAp = new AccessPoint(
                    cyclicMac,
                    gatewayMac,
                    DeviceType.ACCESS_POINT,
                    List.of()
            );
            deviceRepository.save(cyclicAp);
            
            // Now try to update the gateway to point to this device, creating a cycle
            Gateway cyclicGateway = new Gateway(
                    gatewayMac,
                    cyclicMac,
                    DeviceType.GATEWAY,
                    List.of()
            );

            // Act & Assert
            assertThatThrownBy(() -> deviceRepository.save(cyclicGateway))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("circular connection");
        } finally {
            // Clean up the test device
            cleanupTestDevice(cyclicMac);
        }
    }

    @Test
    void fetchSubtree_shouldReturnCompleteSubtree() {
        // Act
        Optional<Device> result = deviceRepository.fetchSubtree(gatewayMac);

        // Assert
        assertThat(result).isPresent();
        Device rootDevice = result.get();
        assertThat(rootDevice.getMacAddress()).isEqualTo(gatewayMac);
        
        // Check first level children
        List<Device> firstLevelChildren = rootDevice.getDownlinkDevices();
        assertThat(firstLevelChildren).hasSize(1);
        assertThat(firstLevelChildren.getFirst().getMacAddress()).isEqualTo(switchMac);
        
        // Check second level children
        List<Device> secondLevelChildren = firstLevelChildren.getFirst().getDownlinkDevices();
        assertThat(secondLevelChildren).hasSize(1);
        assertThat(secondLevelChildren.getFirst().getMacAddress()).isEqualTo(accessPointMac);
        
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
        Optional<Device> result = deviceRepository.fetchSubtree(switchMac);

        // Assert
        assertThat(result).isPresent();
        Device rootDevice = result.get();
        assertThat(rootDevice.getMacAddress()).isEqualTo(switchMac);
        
        // Check children
        List<Device> children = rootDevice.getDownlinkDevices();
        assertThat(children).hasSize(1);
        assertThat(children.getFirst().getMacAddress()).isEqualTo(accessPointMac);
        
        // Verify the parent (gateway) is not included in the subtree
        assertThat(rootDevice.getUplinkMacAddress()).isEqualTo(gatewayMac);
        assertThat(children).extracting("macAddress").doesNotContain(gatewayMac);
    }
}
