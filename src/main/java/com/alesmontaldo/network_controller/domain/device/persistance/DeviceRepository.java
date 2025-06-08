package com.alesmontaldo.network_controller.domain.device.persistance;

import com.alesmontaldo.network_controller.codegen.types.Device;
import com.alesmontaldo.network_controller.domain.device.MacAddress;

import jakarta.validation.ValidationException;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

/**
 * Repository interface for device operations.
 * Defines the contract for device persistence operations regardless of the underlying storage mechanism.
 */
public abstract class DeviceRepository {
    
    /**
     * Finds a device by its MAC address.
     *
     * @param id The MAC address of the device to find
     * @return An Optional containing the device if found, or empty if not found
     */
    public abstract Optional<Device> findById(MacAddress id);
    
    /**
     * Saves a device with topology consistency validation.
     * Implementations should ensure that adding the device won't create cycles in the network topology.
     *
     * @param device The device to save
     * @return The saved device
     * @throws ConcurrentModificationException if unable to acquire a lock
     * @throws ValidationException if adding the device would create a cycle
     */
    @Retryable(
            retryFor = {ConcurrentModificationException.class, ValidationException.class},
            backoff = @Backoff(delay = 500, maxDelay = 2_000)
    )
    public abstract Device save(Device device);
    
    /**
     * Fetches a device and its entire subtree (all descendants).
     *
     * @param rootMac The MAC address of the root node
     * @return An Optional containing the device with its subtree if found, or empty if not found
     */
    public abstract Optional<Device> fetchSubtree(MacAddress rootMac);

    protected void validateEventualNewCycle(Device device) {
        MacAddress mac = device.getMacAddress();
        MacAddress uplinkMac = device.getUplinkMacAddress();

        if (uplinkMac != null) {
            Optional<Device> uplinkDevice = findById(uplinkMac);
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
    protected boolean wouldCreateCycle(MacAddress newDeviceMac, MacAddress directUplinkMac) {
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
            Optional<Device> currentDevice = findById(currentMac);
            if (currentDevice.isEmpty()) {
                // If the device doesn't exist, we can't go further up
                break;
            }

            // Move to the uplink device
            currentMac = currentDevice.get().getUplinkMacAddress();
        }

        return false;
    }
}
