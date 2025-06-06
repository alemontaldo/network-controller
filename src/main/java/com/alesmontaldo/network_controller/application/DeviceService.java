package com.alesmontaldo.network_controller.application;

import static com.alesmontaldo.network_controller.codegen.types.DeviceType.*;

import com.alesmontaldo.network_controller.codegen.types.*;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import com.alesmontaldo.network_controller.domain.device.persistance.DeviceRepository;
import jakarta.validation.ValidationException;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceService {

    private static final Log log = LogFactory.getLog(DeviceService.class);

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public Device getDeviceByMac(MacAddress mac) {
        log.info("Fetching device with mac: " + mac);
        return deviceRepository.findById(mac).orElse(null);
    }

    @Transactional
    public Device addDevice(MacAddress mac, MacAddress uplinkMac, DeviceType deviceType) {
        log.info("Trying to add new device with mac:" + mac + ", uplinkMac: " + uplinkMac + " and deviceType: " + deviceType);

        try {
            checkIfSafeAddition(mac, uplinkMac);

            Device newDevice = switch (deviceType) {
                case GATEWAY -> new Gateway(mac, uplinkMac, GATEWAY, new ArrayList<>());
                case SWITCH -> new Switch(mac, uplinkMac, SWITCH, new ArrayList<>());
                case ACCESS_POINT -> new AccessPoint(mac, uplinkMac, ACCESS_POINT, new ArrayList<>());
            };

            log.info("Adding new device: " + newDevice);
            return deviceRepository.save(newDevice);
        } catch (OptimisticLockingFailureException e) {
            throw new ConcurrentModificationException("The device topology was modified concurrently. Please try again.", e);
        }
    }

    private void checkIfSafeAddition(MacAddress mac, MacAddress uplinkMac) {
        if (uplinkMac != null) {
            if (Objects.equals(mac, uplinkMac)) {
                throw new ValidationException("Device MAC cannot be the same as its uplink MAC");
            }

            Optional<Device> uplinkDevice = deviceRepository.findById(uplinkMac);
            if (uplinkDevice.isEmpty()) {
                throw new ValidationException("Uplink device with MAC: " + uplinkMac + " does not exist");
            }

            if (wouldCreateCycle(mac, uplinkMac)) {
                throw new ValidationException("Adding this device would create a circular connection in the network topology");
            }
        }
    }

    /**
     * Checks if adding a device with the given MAC and uplink MAC would create a cycle in the topology.
     *
     * @param newDeviceMac The MAC of the device being added
     * @param directUplinkMac The uplink MAC of the device being added
     * @return true if adding this device would create a cycle, false otherwise
     */
    private boolean wouldCreateCycle(MacAddress newDeviceMac, MacAddress directUplinkMac) {
        // Start with the direct uplink
        MacAddress currentMac = directUplinkMac;

        // Set to keep track of visited devices to detect cycles
        Set<MacAddress> visitedMacs = new HashSet<>();

        // Traverse up the topology
        while (currentMac != null) {
            // If we've seen this MAC before, or if it's the same as the new device's MAC, we have a cycle
            if (!visitedMacs.add(currentMac) || currentMac.equals(newDeviceMac)) {
                return true;
            }

            // Get the current device's uplink
            Optional<Device> currentDevice = deviceRepository.findById(currentMac);
            if (currentDevice.isEmpty()) {
                // If the device doesn't exist, we can't go further up
                break;
            }

            // Move to the uplink device
            currentMac = currentDevice.get().getUplinkMac();
        }

        return false;
    }

    public Device getSubtree(MacAddress rootMac) {
        return deviceRepository.fetchSubtree(rootMac).orElse(null);
    }

    //Extra feature
    public void deleteDevice(MacAddress mac) {
        log.info("Deleting device with mac: " + mac);
        deviceRepository.deleteById(mac);
    }
}
