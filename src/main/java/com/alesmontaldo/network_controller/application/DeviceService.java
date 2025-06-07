package com.alesmontaldo.network_controller.application;

import static com.alesmontaldo.network_controller.codegen.types.DeviceType.*;

import com.alesmontaldo.network_controller.codegen.types.*;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import com.alesmontaldo.network_controller.domain.device.persistance.DeviceRepository;
import com.alesmontaldo.network_controller.domain.device.persistance.mongo_db.DeviceMongoRepository;
import jakarta.validation.ValidationException;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for device-related business operations.
 * Focuses on business logic while delegating persistence concerns to the repository.
 */
@Service
public class DeviceService {

    private static final Log log = LogFactory.getLog(DeviceService.class);

    private final DeviceRepository deviceRepository;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    /**
     * Retrieves a device by its MAC address.
     *
     * @param mac The MAC address of the device to retrieve
     * @return The device if found, null otherwise
     */
    public Device getDeviceByMac(MacAddress mac) {
        log.info("Fetching device with mac: " + mac);
        return deviceRepository.findById(mac).orElse(null);
    }

    /**
     * Adds a new device to the network.
     * Performs basic validation and creates the appropriate device type.
     * Delegates topology consistency checks and locking to the repository layer.
     *
     * @param mac The MAC address of the new device
     * @param uplinkMac The MAC address of the uplink device (can be null for root devices)
     * @param deviceType The type of device to create
     * @return The newly created device
     */
    public Device addDevice(MacAddress mac, MacAddress uplinkMac, DeviceType deviceType) {
        log.info("Trying to add new device with mac:" + mac + ", uplinkMac: " + uplinkMac + " and deviceType: " + deviceType);

        // Basic validation that doesn't require topology-wide consistency
        if (Objects.equals(mac, uplinkMac)) {
            throw new ValidationException("Device MAC cannot be the same as its uplink MAC");
        }
        
        // Create the appropriate device type
        Device newDevice = switch (deviceType) {
            case GATEWAY -> new Gateway(mac, uplinkMac, GATEWAY, new ArrayList<>());
            case SWITCH -> new Switch(mac, uplinkMac, SWITCH, new ArrayList<>());
            case ACCESS_POINT -> new AccessPoint(mac, uplinkMac, ACCESS_POINT, new ArrayList<>());
        };

        return deviceRepository.save(newDevice);
    }

    /**
     * Retrieves a device and its entire subtree.
     *
     * @param rootMac The MAC address of the root device
     * @return The device with its subtree if found, null otherwise
     */
    public Device getSubtree(MacAddress rootMac) {
        return deviceRepository.fetchSubtree(rootMac).orElse(null);
    }
}
