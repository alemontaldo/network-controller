package com.alesmontaldo.network_controller.application;

import static com.alesmontaldo.network_controller.codegen.types.DeviceType.*;

import com.alesmontaldo.network_controller.codegen.types.*;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import com.alesmontaldo.network_controller.domain.device.persistance.DeviceRepository;
import jakarta.validation.ValidationException;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
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
     * @param macAddress The MAC address of the device to retrieve
     * @throws ValidationException could not find device with given id
     * @return The device found
     */
    @NotNull
    public Device getDeviceByMac(MacAddress macAddress) {
        log.info("Fetching device with mac: " + macAddress);
        Optional<Device> fromDb = deviceRepository.findById(macAddress);
        if (fromDb.isEmpty()) {
            throw new ValidationException("Device with MAC address: " + macAddress + " cannot be found");
        }
        log.info("Found device: " + fromDb);
        return fromDb.get();
    }

    /**
     * Retrieves all devices sorted by deviceType.
     *
     * @return all devices sorted by deviceType
     */
    public List<DeviceResultView> getAllDevicesSorted() {
        List<Device> allDevices = deviceRepository.findAll();

        return allDevices.stream()
                .sorted((d1, d2) -> {
                    Map<DeviceType, Integer> typeOrder = Map.of(
                            DeviceType.GATEWAY, 0,
                            DeviceType.SWITCH, 1,
                            DeviceType.ACCESS_POINT, 2
                    );

                    return Integer.compare(
                            typeOrder.getOrDefault(d1.getDeviceType(), Integer.MAX_VALUE),
                            typeOrder.getOrDefault(d2.getDeviceType(), Integer.MAX_VALUE)
                    );
                })
                .map(device -> new DeviceResultView(device.getMacAddress(), device.getDeviceType()))
                .collect(Collectors.toList());
    }

    /**
     * Adds a new device to the network.
     * Performs basic validation and creates the appropriate device type.
     * Delegates topology consistency checks and locking to the repository layer.
     *
     * @param mac The MAC address of the new device
     * @param uplinkMac The MAC address of the uplink device (can be null for root devices)
     * @param deviceType The type of device to create
     * @throws ConcurrentModificationException if unable to acquire a lock
     * @throws ValidationException if adding the device would create a cycle or if device could not be found
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
     * @return The device with its subtree if found
     * @throws ValidationException if device could not be found
     */
    @NotNull
    public Device getSubtree(MacAddress rootMac) {
        Optional<Device> fromDB = deviceRepository.fetchSubtree(rootMac);
        if (fromDB.isEmpty()) {
            throw new ValidationException("Device with MAC Address: " + rootMac + " was not found");
        } else {
            return fromDB.get();
        }
    }

    @NotNull
    public List<Object> getFullTopology() {
        List<Device> allDevices = getAllDevices();

        // Find root devices (those with no uplink)
        List<Device> rootDevices = allDevices.stream()
                .filter(device -> device.getUplinkMacAddress() == null)
                .toList();

        // Obtain the device topology for each root device
        List<Object> forest = new ArrayList<>();
        for (Device rootDevice : rootDevices) {
            Device completeSubtree = getSubtree(rootDevice.getMacAddress());
            forest.add(buildSimplifiedTopology(completeSubtree));
        }

        return forest;
    }

    /**
     * Builds a simplified topology tree where each node is represented only by its MAC address.
     *
     * @param device The device to convert
     * @return A map representing the device topology
     */
    public Map<String, Object> buildSimplifiedTopology(@NotNull Device device) {
        Map<String, Object> result = new HashMap<>();

        // Use the MAC address as the key, and a list of child devices as the value
        String macAddressStr = device.getMacAddress().getValue();

        // Process downlink devices recursively
        List<Map<String, Object>> children = new ArrayList<>();
        if (device.getDownlinkDevices() != null && !device.getDownlinkDevices().isEmpty()) {
            for (Device child : device.getDownlinkDevices()) {
                children.add(buildSimplifiedTopology(child));
            }
        }

        // Only add the children if there are any
        if (!children.isEmpty()) {
            result.put(macAddressStr, children);
        } else {
            result.put(macAddressStr, Collections.emptyList());
        }

        return result;
    }

    private List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }
}
