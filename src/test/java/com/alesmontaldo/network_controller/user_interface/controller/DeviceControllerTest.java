package com.alesmontaldo.network_controller.user_interface.controller;

import com.alesmontaldo.network_controller.codegen.types.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.client.DgsGraphQlClient;
import org.springframework.graphql.client.HttpSyncGraphQlClient;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DeviceControllerTest {

    @LocalServerPort
    private int port;

    private DgsGraphQlClient dgsGraphQlClient;

    @BeforeEach
    void setUp() {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/graphql")
                .build();
        HttpSyncGraphQlClient graphQlClient = HttpSyncGraphQlClient.builder(restClient).build();
        this.dgsGraphQlClient = DgsGraphQlClient.create(graphQlClient);
    }

    @Test
    void testAddDevice() {
        // Add a gateway device (no uplink)
        String gatewayMac = "AA:AA:AA:AA:AA:AA";
        Map<String, Object> gatewayResult = addDevice(gatewayMac, null, DeviceType.GATEWAY);
        
        assertNotNull(gatewayResult);
        assertEquals(gatewayMac, gatewayResult.get("macAddress"));
        assertEquals("GATEWAY", gatewayResult.get("deviceType"));
        assertNull(gatewayResult.get("uplinkMacAddress"));
        
        // Add a switch connected to the gateway
        String switchMac = "BB:BB:BB:BB:BB:BB";
        Map<String, Object> switchResult = addDevice(switchMac, gatewayMac, DeviceType.SWITCH);
        
        assertNotNull(switchResult);
        assertEquals(switchMac, switchResult.get("macAddress"));
        assertEquals("SWITCH", switchResult.get("deviceType"));
        assertEquals(gatewayMac, switchResult.get("uplinkMacAddress"));
        
        // Add an access point connected to the switch
        String apMac = "CC:CC:CC:CC:CC:CC";
        Map<String, Object> apResult = addDevice(apMac, switchMac, DeviceType.ACCESS_POINT);
        
        assertNotNull(apResult);
        assertEquals(apMac, apResult.get("macAddress"));
        assertEquals("ACCESS_POINT", apResult.get("deviceType"));
        assertEquals(switchMac, apResult.get("uplinkMacAddress"));
    }
    
    @Test
    void testGetDevice() {
        // First add a device
        String macAddress = "DD:DD:DD:DD:DD:DD";
        addDevice(macAddress, null, DeviceType.GATEWAY);
        
        // Then retrieve it
        String query = """
            query {
                getDevice(macAddress: "%s") {
                    ... on DeviceResultView {
                        macAddress
                        deviceType
                    }
                    ... on ValidationError {
                        message
                    }
                    ... on ServerError {
                        message
                        errorCode
                    }
                }
            }
        """.formatted(macAddress);
        
        Map<String, Object> result = dgsGraphQlClient.executeSync(query)
                .extractValueAsObject("getDevice", Map.class);
        
        assertNotNull(result);
        assertEquals(macAddress, result.get("macAddress"));
        assertEquals("GATEWAY", result.get("deviceType"));
    }
    
    @Test
    void testAllDevicesSorted() {
        // Add devices of different types
        addDevice("EE:EE:EE:EE:EE:EE", null, DeviceType.GATEWAY);
        addDevice("FF:FF:FF:FF:FF:FF", "EE:EE:EE:EE:EE:EE", DeviceType.SWITCH);
        addDevice("GG:GG:GG:GG:GG:GG", "FF:FF:FF:FF:FF:FF", DeviceType.ACCESS_POINT);
        
        String query = """
            query {
                allDevicesSorted {
                    ... on DeviceResultView {
                        macAddress
                        deviceType
                    }
                    ... on ServerError {
                        message
                        errorCode
                    }
                }
            }
        """;
        
        List<Map<String, Object>> results = dgsGraphQlClient.executeSync(query)
                .extractValueAsObject("allDevicesSorted", List.class);
        
        assertNotNull(results);
        assertTrue(results.size() >= 3);
        
        // Verify sorting order: GATEWAY > SWITCH > ACCESS_POINT
        boolean foundGateway = false;
        boolean foundSwitch = false;
        
        for (Map<String, Object> device : results) {
            String deviceType = (String) device.get("deviceType");
            
            if ("GATEWAY".equals(deviceType)) {
                foundGateway = true;
                assertFalse(foundSwitch, "Gateways should come before Switches");
            } else if ("SWITCH".equals(deviceType)) {
                foundSwitch = true;
            } else if ("ACCESS_POINT".equals(deviceType)) {
                assertTrue(foundGateway && foundSwitch, "Access Points should come after Gateways and Switches");
            }
        }
    }
    
    @Test
    void testFullTopology() {
        // Add a simple network topology
        String gatewayMac = "HH:HH:HH:HH:HH:HH";
        String switchMac = "II:II:II:II:II:II";
        String apMac = "JJ:JJ:JJ:JJ:JJ:JJ";
        
        addDevice(gatewayMac, null, DeviceType.GATEWAY);
        addDevice(switchMac, gatewayMac, DeviceType.SWITCH);
        addDevice(apMac, switchMac, DeviceType.ACCESS_POINT);
        
        String query = """
            query {
                fullTopology {
                    ... on JsonResult {
                        data
                    }
                    ... on ValidationError {
                        message
                    }
                    ... on ServerError {
                        message
                        errorCode
                    }
                }
            }
        """;
        
        Map<String, Object> result = dgsGraphQlClient.executeSync(query)
                .extractValueAsObject("fullTopology", Map.class);
        
        assertNotNull(result);
        assertNotNull(result.get("data"));
        
        // The data field contains a JSON representation of the topology
        Object topologyData = result.get("data");
        assertNotNull(topologyData);
        
        // Since this is a JSON object, we can't easily assert on its structure in this test
        // But we can verify it's not empty
        String topologyString = topologyData.toString();
        assertTrue(topologyString.contains(gatewayMac.replace(":", "")));
    }
    
    @Test
    void testDeviceTopology() {
        // Add a simple network topology
        String gatewayMac = "KK:KK:KK:KK:KK:KK";
        String switchMac = "LL:LL:LL:LL:LL:LL";
        String apMac = "MM:MM:MM:MM:MM:MM";
        
        addDevice(gatewayMac, null, DeviceType.GATEWAY);
        addDevice(switchMac, gatewayMac, DeviceType.SWITCH);
        addDevice(apMac, switchMac, DeviceType.ACCESS_POINT);
        
        String query = """
            query {
                deviceTopology(macAddress: "%s") {
                    ... on JsonResult {
                        data
                    }
                    ... on ValidationError {
                        message
                    }
                    ... on ServerError {
                        message
                        errorCode
                    }
                }
            }
        """.formatted(gatewayMac);
        
        Map<String, Object> result = dgsGraphQlClient.executeSync(query)
                .extractValueAsObject("deviceTopology", Map.class);
        
        assertNotNull(result);
        assertNotNull(result.get("data"));
        
        // The data field contains a JSON representation of the subtree
        Object topologyData = result.get("data");
        assertNotNull(topologyData);
        
        // Since this is a JSON object, we can't easily assert on its structure in this test
        // But we can verify it's not empty and contains our devices
        String topologyString = topologyData.toString();
        assertTrue(topologyString.contains(gatewayMac.replace(":", "")));
    }
    
    @Test
    void testValidationError() {
        // Try to get a device with an invalid MAC address
        String invalidMac = "invalid-mac";
        
        String query = """
            query {
                getDevice(macAddress: "%s") {
                    ... on DeviceResultView {
                        macAddress
                        deviceType
                    }
                    ... on ValidationError {
                        message
                    }
                    ... on ServerError {
                        message
                        errorCode
                    }
                }
            }
        """.formatted(invalidMac);
        
        Map<String, Object> result = dgsGraphQlClient.executeSync(query)
                .extractValueAsObject("getDevice", Map.class);
        
        assertNotNull(result);
        assertNotNull(result.get("message"));
        assertNull(result.get("macAddress"));
    }
    
    // Helper method to add a device and return the result
    private Map<String, Object> addDevice(String macAddress, String uplinkMacAddress, DeviceType deviceType) {
        String mutation = """
            mutation {
                addDevice(input: {
                    macAddress: "%s",
                    %s
                    deviceType: %s
                }) {
                    ... on Gateway {
                        macAddress
                        uplinkMacAddress
                        deviceType
                    }
                    ... on Switch {
                        macAddress
                        uplinkMacAddress
                        deviceType
                    }
                    ... on AccessPoint {
                        macAddress
                        uplinkMacAddress
                        deviceType
                    }
                    ... on ValidationError {
                        message
                    }
                    ... on ServerError {
                        message
                        errorCode
                    }
                }
            }
        """.formatted(
                macAddress,
                uplinkMacAddress != null ? "uplinkMacAddress: \"" + uplinkMacAddress + "\"," : "",
                deviceType.name()
        );
        
        return dgsGraphQlClient.executeSync(mutation)
                .extractValueAsObject("addDevice", Map.class);
    }
}
