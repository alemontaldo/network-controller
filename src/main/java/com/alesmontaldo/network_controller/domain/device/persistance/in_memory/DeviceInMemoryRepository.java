package com.alesmontaldo.network_controller.domain.device.persistance.in_memory;

import com.alesmontaldo.network_controller.codegen.types.*;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import com.alesmontaldo.network_controller.domain.device.persistance.DeviceRepository;
import jakarta.validation.ValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of the DeviceRepository interface.
 * Uses thread-safe collections to store devices and provides the same functionality
 * as the MongoDB implementation but without requiring a database.
 */
@Repository
@Profile("in-memory")
public class DeviceInMemoryRepository extends DeviceRepository {

    private static final Log log = LogFactory.getLog(DeviceInMemoryRepository.class);
    
    private final Map<MacAddress, Device> devices = new ConcurrentHashMap<>();
    private final Object lockObject = new Object();

    @Override
    public Optional<Device> findById(MacAddress id) {
        return Optional.ofNullable(devices.get(id));
    }

    @Override
    public Device save(Device device) {
        synchronized(lockObject) {
            log.info("Validating device addition for an eventual new cycle for device: " + device);
            validateEventualNewCycle(device);
            
            log.info("Adding new device: " + device);
            // Create a deep copy to avoid reference issues
            Device savedDevice = cloneDevice(device);
            devices.put(device.getMac(), savedDevice);
            return savedDevice;
        }
    }

    @Override
    public Optional<Device> fetchSubtree(MacAddress rootMac) {
        // First, check if the root device exists
        Optional<Device> rootDeviceOpt = findById(rootMac);
        if (rootDeviceOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Device rootDevice = rootDeviceOpt.get();
        
        // Find all devices in the repository
        List<Device> allDevices = new ArrayList<>(devices.values());
        
        // Build the device hierarchy using recursion and immutable objects
        return Optional.of(buildDeviceHierarchy(rootDevice, allDevices));
    }
    
    /**
     * Recursively builds the device hierarchy by finding and attaching child devices.
     * Since Device objects are immutable, we create new instances with the proper children.
     *
     * @param device The current device to process
     * @param allDevices List of all devices in the repository
     * @return A new Device instance with the complete hierarchy
     */
    private Device buildDeviceHierarchy(Device device, List<Device> allDevices) {
        // Find direct children of this device
        List<Device> directChildren = allDevices.stream()
                .filter(child -> child.getUplinkMac() != null && 
                       child.getUplinkMac().equals(device.getMac()))
                .map(child -> buildDeviceHierarchy(child, allDevices)) // Recursively build each child's hierarchy
                .collect(Collectors.toList());
        
        // Create a new device instance with the same properties but with the children attached
        return createDeviceWithChildren(device, directChildren);
    }
    
    /**
     * Creates a deep copy of a device to avoid reference issues.
     * This is necessary because we're storing objects in memory.
     *
     * @param device The device to clone
     * @return A new device instance with the same properties
     */
    private Device cloneDevice(Device device) {
        // Create a new device with the same properties but with empty children
        return createDeviceWithChildren(device, Collections.emptyList());
    }
    
    /**
     * Creates a new device instance with the same properties as the original but with the specified children.
     * This handles the different concrete device types.
     *
     * @param device The original device
     * @param children The children to attach to the new device
     * @return A new device instance
     */
    private Device createDeviceWithChildren(Device device, List<Device> children) {
        return switch (device.getDeviceType()) {
            case GATEWAY -> new Gateway(
                    device.getMac(),
                    device.getUplinkMac(),
                    DeviceType.GATEWAY,
                    children
            );
            case SWITCH -> new Switch(
                    device.getMac(),
                    device.getUplinkMac(),
                    DeviceType.SWITCH,
                    children
            );
            case ACCESS_POINT -> new AccessPoint(
                    device.getMac(),
                    device.getUplinkMac(),
                    DeviceType.ACCESS_POINT,
                    children
            );
        };
    }
}
