package com.alesmontaldo.network_controller.user_interface.controller;

import com.alesmontaldo.network_controller.application.DeviceService;
import com.alesmontaldo.network_controller.codegen.types.DeleteDeviceResponse;
import com.alesmontaldo.network_controller.codegen.types.Device;
import com.alesmontaldo.network_controller.codegen.types.DeviceType;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import java.util.Map;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;

@Controller
public class DeviceController {

    private final DeviceService deviceService;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 500;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @QueryMapping
    public Device deviceByMac(@Argument MacAddress mac) {
        return deviceService.getDeviceByMac(mac);
    }

    @QueryMapping
    public Device subtree(@Argument MacAddress mac) {
        return deviceService.getSubtree(mac);
    }

    @MutationMapping
    public Device addDevice(@Argument("input") Map<String, Object> input) {
        MacAddress mac = (MacAddress) input.get("mac");
        MacAddress uplinkMac = (MacAddress) input.get("uplinkMac");
        DeviceType deviceType = DeviceType.valueOf((String) input.get("deviceType"));

        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                return deviceService.addDevice(mac, uplinkMac, deviceType);
            } catch (ConcurrentModificationException e) {
                attempts++;
                if (attempts >= MAX_RETRIES) {
                    throw e;
                }
                
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Operation interrupted", ie);
                }
            }
        }
        throw new RuntimeException("Failed to add device after " + MAX_RETRIES + " attempts");
    }

    //Extra feature
    @MutationMapping
    public DeleteDeviceResponse deleteDevice(@Argument("mac") MacAddress mac) {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                deviceService.deleteDevice(mac);
                // only happy path here
                return new DeleteDeviceResponse(true, "Device successfully deleted", mac);
            } catch (ConcurrentModificationException e) {
                attempts++;
                if (attempts >= MAX_RETRIES) {
                    return new DeleteDeviceResponse(false, "Failed to delete device: " + e.getMessage(), mac);
                }
                
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Operation interrupted", ie);
                }
            }
        }
        return new DeleteDeviceResponse(false, "Failed to delete device after " + MAX_RETRIES + " attempts", mac);
    }
}
