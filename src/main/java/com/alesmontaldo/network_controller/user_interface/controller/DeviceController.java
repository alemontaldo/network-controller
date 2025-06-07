package com.alesmontaldo.network_controller.user_interface.controller;

import com.alesmontaldo.network_controller.application.DeviceService;
import com.alesmontaldo.network_controller.codegen.types.DeleteDeviceResponse;
import com.alesmontaldo.network_controller.codegen.types.Device;
import com.alesmontaldo.network_controller.codegen.types.DeviceType;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import java.util.ConcurrentModificationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class DeviceController {

    private static final Log log = LogFactory.getLog(DeviceController.class);

    private final DeviceService deviceService;

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
    @Retryable(
            retryFor = ConcurrentModificationException.class,
            backoff = @Backoff(delay = 500)
    )
    public Device addDevice(@Argument("input") Map<String, Object> input) {
        MacAddress mac = (MacAddress) input.get("mac");
        MacAddress uplinkMac = (MacAddress) input.get("uplinkMac");
        DeviceType deviceType = DeviceType.valueOf((String) input.get("deviceType"));

        return deviceService.addDevice(mac, uplinkMac, deviceType);
    }

    @Recover
    public Device recoverAddDevice(ConcurrentModificationException e, Map<String, Object> input) {
        log.error("Could not add device after multiple retries. Perhaps concurrent updates to network topology are being attempted");
        throw new RuntimeException("Failed to add device after multiple attempts: " + e.getMessage(), e);
    }

    //Extra feature
    @MutationMapping
    @Retryable(
            retryFor = ConcurrentModificationException.class,
            backoff = @Backoff(delay = 500)
    )
    public DeleteDeviceResponse deleteDevice(@Argument("mac") MacAddress mac) {
        deviceService.deleteDevice(mac);
        // only happy path here
        return new DeleteDeviceResponse(true, "Device successfully deleted", mac);
    }
    
    @Recover
    public DeleteDeviceResponse recoverDeleteDevice(ConcurrentModificationException e, MacAddress mac) {
        log.error("Could not delete device after multiple retries. Perhaps concurrent updates to network topology are being attempted");
        return new DeleteDeviceResponse(false, "Failed to delete device after multiple attempts: " + e.getMessage(), mac);
    }
}
