package com.alesmontaldo.network_controller.domain.device.persistance;

import com.alesmontaldo.network_controller.codegen.types.Device;
import com.alesmontaldo.network_controller.domain.device.MacAddress;

import jakarta.validation.ValidationException;
import java.util.ConcurrentModificationException;
import java.util.Optional;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

/**
 * Repository interface for device operations.
 * Defines the contract for device persistence operations regardless of the underlying storage mechanism.
 */
public interface DeviceRepository {
    
    /**
     * Finds a device by its MAC address.
     *
     * @param id The MAC address of the device to find
     * @return An Optional containing the device if found, or empty if not found
     */
    Optional<Device> findById(MacAddress id);
    
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
    Device save(Device device);
    
    /**
     * Fetches a device and its entire subtree (all descendants).
     *
     * @param rootMac The MAC address of the root node
     * @return An Optional containing the device with its subtree if found, or empty if not found
     */
    Optional<Device> fetchSubtree(MacAddress rootMac);
}
