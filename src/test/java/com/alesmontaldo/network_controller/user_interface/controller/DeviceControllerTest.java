package com.alesmontaldo.network_controller.user_interface.controller;

import com.alesmontaldo.network_controller.codegen.types.*;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.graphql.GraphQlResponse;
import org.springframework.graphql.client.HttpSyncGraphQlClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("mongo")
@TestPropertySource(properties = {
        "spring.data.mongodb.database=network-controller_test_db"
})
@Import(RestClientAutoConfiguration.class)
public class DeviceControllerTest {

    private static final Log log = LogFactory.getLog(DeviceControllerTest.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Autowired
    RestClient.Builder builder;
    @Autowired
    private MongoRepository mongoRepository;

    private HttpSyncGraphQlClient client;

    @BeforeEach
    void setUp() {
        mongoRepository.deleteAll();

        RestClient restClient = builder.baseUrl("http://localhost:" + port + "/graphql").build();
        this.client = HttpSyncGraphQlClient.builder(restClient).build();
    }

    @AfterEach
    void tearDown() {
        mongoRepository.deleteAll();
    }

    // Helper method to generate unique MAC addresses for tests
    private String generateUniqueMac() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid.substring(0, 2) + ":" + 
               uuid.substring(2, 4) + ":" + 
               uuid.substring(4, 6) + ":" + 
               uuid.substring(6, 8) + ":" + 
               uuid.substring(8, 10) + ":" + 
               uuid.substring(10, 12);
    }

    // ========== ADD DEVICE TESTS ==========

    @Test
    void testAddGatewayDevice_Success() {
        // Add a gateway device (no uplink)
        String uniqueMac = generateUniqueMac();
        MacAddress gatewayMac = new MacAddress(uniqueMac);

        DeviceInput deviceInput = new DeviceInput();
        deviceInput.setMacAddress(gatewayMac);
        deviceInput.setDeviceType(DeviceType.GATEWAY);

        GraphQlResponse response = this.client.documentName("addDevice")
                .variable("input", deviceInput)
                .executeSync();
        
        Map<String, Object> responseData = response.getData();
        Map<String, Object> addDeviceData = (Map<String, Object>) responseData.get("addDevice");
        
        log.info("Add Gateway Result: " + addDeviceData);
        
        // Check if we have a successful response (Gateway)
        assertNotNull(addDeviceData.get("macAddress"));
        assertNotNull(addDeviceData.get("deviceType"));
        assertEquals(uniqueMac.toUpperCase(), addDeviceData.get("macAddress").toString().toUpperCase());
        assertEquals(DeviceType.GATEWAY.toString(), addDeviceData.get("deviceType").toString());
        assertNull(addDeviceData.get("uplinkMacAddress"));
    }

    @Test
    void testAddSwitchDevice_Success() {
        // First add a gateway as uplink
        String gatewayMac = generateUniqueMac();
        addDevice(gatewayMac, null, DeviceType.GATEWAY);
        
        // Then add a switch with uplink to the gateway
        String switchMac = generateUniqueMac();
        
        DeviceInput deviceInput = new DeviceInput();
        deviceInput.setMacAddress(new MacAddress(switchMac));
        deviceInput.setDeviceType(DeviceType.SWITCH);
        deviceInput.setUplinkMacAddress(new MacAddress(gatewayMac));

        GraphQlResponse response = this.client.documentName("addDevice")
                .variable("input", deviceInput)
                .executeSync();
        
        Map<String, Object> responseData = response.getData();
        Map<String, Object> addDeviceData = (Map<String, Object>) responseData.get("addDevice");
        
        log.info("Add Switch Result: " + addDeviceData);
        
        // Check if we have a successful response (Switch)
        assertNotNull(addDeviceData.get("macAddress"));
        assertNotNull(addDeviceData.get("deviceType"));
        assertEquals(switchMac.toUpperCase(), addDeviceData.get("macAddress").toString().toUpperCase());
        assertEquals(DeviceType.SWITCH.toString(), addDeviceData.get("deviceType").toString());
        assertEquals(gatewayMac.toUpperCase(), addDeviceData.get("uplinkMacAddress").toString().toUpperCase());
    }

    @Test
    void testAddAccessPointDevice_Success() {
        // First add a gateway and switch as uplink chain
        String gatewayMac = generateUniqueMac();
        addDevice(gatewayMac, null, DeviceType.GATEWAY);
        
        String switchMac = generateUniqueMac();
        addDevice(switchMac, gatewayMac, DeviceType.SWITCH);
        
        // Then add an access point with uplink to the switch
        String apMac = generateUniqueMac();
        
        DeviceInput deviceInput = new DeviceInput();
        deviceInput.setMacAddress(new MacAddress(apMac));
        deviceInput.setDeviceType(DeviceType.ACCESS_POINT);
        deviceInput.setUplinkMacAddress(new MacAddress(switchMac));

        GraphQlResponse response = this.client.documentName("addDevice")
                .variable("input", deviceInput)
                .executeSync();
        
        Map<String, Object> responseData = response.getData();
        Map<String, Object> addDeviceData = (Map<String, Object>) responseData.get("addDevice");
        
        log.info("Add Access Point Result: " + addDeviceData);
        
        // Check if we have a successful response (AccessPoint)
        assertNotNull(addDeviceData.get("macAddress"));
        assertNotNull(addDeviceData.get("deviceType"));
        assertEquals(apMac.toUpperCase(), addDeviceData.get("macAddress").toString().toUpperCase());
        assertEquals(DeviceType.ACCESS_POINT.toString(), addDeviceData.get("deviceType").toString());
        assertEquals(switchMac.toUpperCase(), addDeviceData.get("uplinkMacAddress").toString().toUpperCase());
    }

    @Test
    void testAddDevice_ValidationError_NonExistentUplink() {
        String deviceMac = generateUniqueMac();
        String nonExistentUplinkMac = generateUniqueMac();
        
        DeviceInput deviceInput = new DeviceInput();
        deviceInput.setMacAddress(new MacAddress(deviceMac));
        deviceInput.setDeviceType(DeviceType.SWITCH);
        deviceInput.setUplinkMacAddress(new MacAddress(nonExistentUplinkMac));

        GraphQlResponse response = this.client.documentName("addDevice")
                .variable("input", deviceInput)
                .executeSync();
        
        Map<String, Object> responseData = response.getData();
        Map<String, Object> addDeviceData = (Map<String, Object>) responseData.get("addDevice");
        
        log.info("Add Device Non-Existent Uplink Result: " + addDeviceData);
        
        // Check if we have an error response
        assertNotNull(addDeviceData.get("message"));
        assertNull(addDeviceData.get("macAddress"));
        String errorMessage = addDeviceData.get("message").toString();
        assertTrue(errorMessage.toLowerCase().contains("uplink") ||
                   errorMessage.toLowerCase().contains("not found") ||
                   errorMessage.toLowerCase().contains("doesn't exist"));
    }

    @Test
    void testAddDevice_ValidationError_CyclicTopology() {
        // Create a gateway
        String gatewayMac = generateUniqueMac();
        addDevice(gatewayMac, null, DeviceType.GATEWAY);
        
        // Create a switch with uplink to gateway
        String switchMac = generateUniqueMac();
        addDevice(switchMac, gatewayMac, DeviceType.SWITCH);
        
        // Try to make gateway uplink to switch (creating a cycle)
        DeviceInput deviceInput = new DeviceInput();
        deviceInput.setMacAddress(new MacAddress(gatewayMac));
        deviceInput.setDeviceType(DeviceType.GATEWAY);
        deviceInput.setUplinkMacAddress(new MacAddress(switchMac));

        GraphQlResponse response = this.client.documentName("addDevice")
                .variable("input", deviceInput)
                .executeSync();
        
        Map<String, Object> responseData = response.getData();
        Map<String, Object> addDeviceData = (Map<String, Object>) responseData.get("addDevice");
        
        log.info("Add Device Cyclic Topology Result: " + addDeviceData);
        
        // Check if we have an error response
        assertNotNull(addDeviceData.get("message"));
        assertNull(addDeviceData.get("macAddress"));
        String errorMessage = addDeviceData.get("message").toString().toLowerCase();
        assertTrue(errorMessage.contains("cycle") || 
                   errorMessage.contains("circular") ||
                   errorMessage.contains("loop"));
    }

    // ========== GET DEVICE TESTS ==========

    @Test
    void testGetDevice_Success() {
        // First add a device
        String deviceMac = generateUniqueMac();
        addDevice(deviceMac, null, DeviceType.GATEWAY);
        
        // Then get the device
        GraphQlResponse response = this.client.documentName("getDevice")
                .variable("input", new MacAddress(deviceMac))
                .executeSync();
        
        Map<String, Object> responseData = response.getData();
        Map<String, Object> getDeviceData = (Map<String, Object>) responseData.get("getDevice");
        
        log.info("Get Device Result: " + getDeviceData);
        
        // Check if we have a successful response
        assertNotNull(getDeviceData.get("macAddress"));
        assertNotNull(getDeviceData.get("deviceType"));
        assertEquals(deviceMac.toUpperCase(), getDeviceData.get("macAddress").toString().toUpperCase());
        assertEquals(DeviceType.GATEWAY.toString(), getDeviceData.get("deviceType").toString());
    }

    @Test
    void testGetDevice_ValidationError_NonExistentDevice() {
        String nonExistentMac = generateUniqueMac();
        
        GraphQlResponse response = this.client.documentName("getDevice")
                .variable("input", new MacAddress(nonExistentMac))
                .executeSync();
        
        Map<String, Object> responseData = response.getData();
        Map<String, Object> getDeviceData = (Map<String, Object>) responseData.get("getDevice");
        
        log.info("Get Non-Existent Device Result: " + getDeviceData);
        
        // Check if we have an error response
        assertNotNull(getDeviceData.get("message"));
        assertNull(getDeviceData.get("macAddress"));
        String errorMessage = getDeviceData.get("message").toString();
        assertTrue(errorMessage.toLowerCase().contains("not found") ||
                errorMessage.toLowerCase().contains("doesn't exist") ||
                errorMessage.toLowerCase().contains("cannot be found"));
    }

    // ========== ALL DEVICES SORTED TESTS ==========

    @Test
    void testAllDevicesSorted_Success() {
        // Add devices of different types
        String gatewayMac = generateUniqueMac();
        addDevice(gatewayMac, null, DeviceType.GATEWAY);
        
        String switchMac = generateUniqueMac();
        addDevice(switchMac, gatewayMac, DeviceType.SWITCH);
        
        String apMac = generateUniqueMac();
        addDevice(apMac, switchMac, DeviceType.ACCESS_POINT);
        
        // Get all devices sorted
        GraphQlResponse response = this.client.documentName("allDevicesSorted")
                .executeSync();
        
        Map<String, Object> responseData = response.getData();
        List<Map<String, Object>> allDevicesData = (List<Map<String, Object>>) responseData.get("allDevicesSorted");
        
        log.info("All Devices Sorted Results: " + allDevicesData);
        
        // Assert we have at least 3 devices
        assertThat(allDevicesData).hasSizeGreaterThanOrEqualTo(3);
        
        // Check sorting order: GATEWAY > SWITCH > ACCESS_POINT
        DeviceType previousType = null;
        for (Map<String, Object> deviceData : allDevicesData) {
            assertNotNull(deviceData.get("macAddress"));
            assertNotNull(deviceData.get("deviceType"));
            
            DeviceType currentType = DeviceType.valueOf(deviceData.get("deviceType").toString());
            
            if (previousType != null) {
                // Check that the order is maintained (or same type)
                assertTrue(previousType.ordinal() <= currentType.ordinal());
            }
            previousType = currentType;
        }
    }

    // ========== FULL TOPOLOGY TESTS ==========

    @Test
    void testFullTopology_Success() {
        // Create a small network
        String gateway1Mac = generateUniqueMac();
        addDevice(gateway1Mac, null, DeviceType.GATEWAY);
        
        String switch1Mac = generateUniqueMac();
        addDevice(switch1Mac, gateway1Mac, DeviceType.SWITCH);
        
        String ap1Mac = generateUniqueMac();
        addDevice(ap1Mac, switch1Mac, DeviceType.ACCESS_POINT);
        
        // Create another separate network
        String gateway2Mac = generateUniqueMac();
        addDevice(gateway2Mac, null, DeviceType.GATEWAY);
        
        // Get full topology
        GraphQlResponse response = this.client.documentName("fullTopology")
                .executeSync();
        
        Map<String, Object> responseData = response.getData();
        Map<String, Object> fullTopologyData = (Map<String, Object>) responseData.get("fullTopology");
        
        log.info("Full Topology Result: " + fullTopologyData);
        
        // Check if we have a successful response
        assertNotNull(fullTopologyData.get("data"));
        
        // Parse the JSON to verify structure
        try {
            Object jsonData = fullTopologyData.get("data");
            JsonNode topologyJson = objectMapper.valueToTree(jsonData);
            assertTrue(topologyJson.isArray());
            assertThat(topologyJson.size()).isGreaterThanOrEqualTo(2); // At least our two gateway trees
        } catch (Exception e) {
            fail("Failed to parse topology JSON: " + e.getMessage());
        }
    }

    // ========== DEVICE TOPOLOGY TESTS ==========

    @Test
    void testDeviceTopology_Success() {
        // Create a small network
        String gatewayMac = generateUniqueMac();
        addDevice(gatewayMac, null, DeviceType.GATEWAY);
        
        String switchMac = generateUniqueMac();
        addDevice(switchMac, gatewayMac, DeviceType.SWITCH);
        
        String ap1Mac = generateUniqueMac();
        addDevice(ap1Mac, switchMac, DeviceType.ACCESS_POINT);
        
        String ap2Mac = generateUniqueMac();
        addDevice(ap2Mac, switchMac, DeviceType.ACCESS_POINT);
        
        // Get topology starting from the switch
        GraphQlResponse response = this.client.documentName("deviceTopology")
                .variable("input", new MacAddress(switchMac))
                .executeSync();
        
        Map<String, Object> responseData = response.getData();
        Map<String, Object> deviceTopologyData = (Map<String, Object>) responseData.get("deviceTopology");
        
        log.info("Device Topology Result: " + deviceTopologyData);
        
        // Check if we have a successful response
        assertNotNull(deviceTopologyData.get("data"));
        
        // Parse the JSON to verify structure
        try {
            Object jsonData = deviceTopologyData.get("data");
            JsonNode topologyJson = objectMapper.valueToTree(jsonData);
            
            // The structure is a simple map with MAC address as key and array of children
            assertTrue(topologyJson.has(switchMac.toUpperCase()), "Should have the switch MAC as key");
            
            // Check that the switch has 2 children (the access points)
            JsonNode children = topologyJson.get(switchMac.toUpperCase());
            assertTrue(children.isArray(), "Children should be an array");
            assertEquals(2, children.size(), "Should have 2 access points as children");
            
            // Verify the children are the access points we added
            boolean foundAp1 = false;
            boolean foundAp2 = false;
            for (JsonNode child : children) {
                if (child.get(ap1Mac.toUpperCase()) != null) foundAp1 = true;
                if (child.get(ap2Mac.toUpperCase()) != null) foundAp2 = true;
            }
            assertTrue(foundAp1, "Should contain ap1Mac in children");
            assertTrue(foundAp2, "Should contain ap2Mac in children");
        } catch (Exception e) {
            fail("Failed to parse topology JSON: " + e.getMessage());
        }
    }

    @Test
    void testDeviceTopology_ValidationError_NonExistentDevice() {
        String nonExistentMac = generateUniqueMac();
        
        GraphQlResponse response = this.client.documentName("deviceTopology")
                .variable("input", new MacAddress(nonExistentMac))
                .executeSync();
        
        Map<String, Object> responseData = response.getData();
        Map<String, Object> deviceTopologyData = (Map<String, Object>) responseData.get("deviceTopology");
        
        log.info("Device Topology Non-Existent Device Result: " + deviceTopologyData);
        
        // Check if we have an error response
        assertNotNull(deviceTopologyData.get("message"));
        assertNull(deviceTopologyData.get("data"));
        String errorMessage = deviceTopologyData.get("message").toString();
        assertTrue(errorMessage.contains("not found") || 
                   errorMessage.contains("doesn't exist"));
    }

    // ========== HELPER METHODS ==========

    /**
     * Helper method to add a device
     */
    private Map<String, Object> addDevice(String macAddress, String uplinkMacAddress, DeviceType deviceType) {
        DeviceInput deviceInput = new DeviceInput();
        deviceInput.setMacAddress(new MacAddress(macAddress));
        deviceInput.setDeviceType(deviceType);
        
        if (uplinkMacAddress != null) {
            deviceInput.setUplinkMacAddress(new MacAddress(uplinkMacAddress));
        }

        GraphQlResponse response = this.client.documentName("addDevice")
                .variable("input", deviceInput)
                .executeSync();
        
        Map<String, Object> responseData = response.getData();
        return (Map<String, Object>) responseData.get("addDevice");
    }
}
