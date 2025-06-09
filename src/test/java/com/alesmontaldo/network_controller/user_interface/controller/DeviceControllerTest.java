package com.alesmontaldo.network_controller.user_interface.controller;

import com.alesmontaldo.network_controller.codegen.types.AddDeviceResult;
import com.alesmontaldo.network_controller.codegen.types.DeviceInput;
import com.alesmontaldo.network_controller.codegen.types.DeviceType;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.client.HttpSyncGraphQlClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("mongo")
@Import(RestClientAutoConfiguration.class)
public class DeviceControllerTest {

    private static final Log log = LogFactory.getLog(DeviceControllerTest.class);

    @LocalServerPort
    private int port;

    @Autowired
    RestClient.Builder builder;
    private HttpSyncGraphQlClient client;

    @BeforeEach
    void setUp() {
        RestClient restClient = builder.baseUrl("http://localhost:" + port + "/graphql").build();
        this.client = HttpSyncGraphQlClient.builder(restClient).build();
    }

    @Test
    void testAddDevice() {
        // Add a gateway device (no uplink)
        String gatewayMacStr = "AA:AA:AA:AA:AA:AA";
        MacAddress gatewayMac = new MacAddress(gatewayMacStr);


        DeviceInput deviceInput = new DeviceInput();
        deviceInput.setMacAddress(gatewayMac);
        deviceInput.setDeviceType(DeviceType.GATEWAY);

        AddDeviceResult addDeviceResult = this.client.documentName("addDevice")
                .variable("input", deviceInput)
                .executeSync()
                .toEntity(AddDeviceResult.class);

        log.info("addDeviceResult: " + addDeviceResult);

    }
}
