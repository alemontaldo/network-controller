package com.alesmontaldo.network_controller.domain.device.persistance.mongo_db;

import com.alesmontaldo.network_controller.codegen.types.DeviceType;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import com.alesmontaldo.network_controller.domain.device.persistance.AbstractDeviceRepositoryTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("mongo")
public class DeviceMongoRepositoryTest extends AbstractDeviceRepositoryTest {

    @Autowired
    private MongoRepository mongoRepository;

    private GatewayDocument testGateway;
    private SwitchDocument testSwitch;
    private AccessPointDocument testAccessPoint;

    @BeforeEach
    void setUp() {
        mongoRepository.deleteAll();
        setupTestData();
    }

    @AfterEach
    void tearDown() {
        mongoRepository.deleteAll();
    }

    @Override
    protected void setupTestData() {
        // Create and save a test gateway
        testGateway = new GatewayDocument();
        testGateway.setMacAddress(gatewayMac);
        testGateway.setUplinkMacAddress(null);
        testGateway.setDeviceType(DeviceType.GATEWAY);
        testGateway = mongoRepository.save(testGateway);

        // Create and save a test switch connected to the gateway
        testSwitch = new SwitchDocument();
        testSwitch.setMacAddress(switchMac);
        testSwitch.setUplinkMacAddress(testGateway.getMacAddress());
        testSwitch.setDeviceType(DeviceType.SWITCH);
        testSwitch = mongoRepository.save(testSwitch);

        // Create and save a test access point connected to the switch
        testAccessPoint = new AccessPointDocument();
        testAccessPoint.setMacAddress(accessPointMac);
        testAccessPoint.setUplinkMacAddress(testSwitch.getMacAddress());
        testAccessPoint.setDeviceType(DeviceType.ACCESS_POINT);
        testAccessPoint = mongoRepository.save(testAccessPoint);
    }

    @Override
    protected void cleanupTestDevice(MacAddress mac) {
        mongoRepository.deleteById(mac);
    }
}
