package com.alesmontaldo.network_controller.user_interface.controller;

import com.alesmontaldo.network_controller.application.DeviceService;
import com.alesmontaldo.network_controller.codegen.types.*;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import jakarta.validation.ValidationException;
import java.util.*;
import java.util.ConcurrentModificationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;

@Controller
public class DeviceController {

    private static final Log log = LogFactory.getLog(DeviceController.class);

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @QueryMapping
    public Device deviceByMac(@Argument MacAddress macAddress) {
        return deviceService.getDeviceByMac(macAddress);
    }

    @QueryMapping
    public Object deviceTopology(@Argument MacAddress macAddress) {
        try {
            Device device = deviceService.getSubtree(macAddress);
            return deviceService.buildSimplifiedTopology(device);
        } catch (ValidationException e) {
            log.warn("Validation error when getting device topology: " + e.getMessage());
            return new ValidationError(e.getMessage());
        }
    }

    @MutationMapping
    public Object addDevice(@Argument("input") DeviceInput deviceInput) {
        try {
            return deviceService.addDevice(deviceInput.getMacAddress(), deviceInput.getUplinkMacAddress(), deviceInput.getDeviceType());
        } catch (ValidationException e) {
            log.warn("Validation error when adding device: " + e.getMessage());
            return new ValidationError(e.getMessage());
        } catch (ConcurrentModificationException e) {
            log.error("Concurrent modification error when adding device", e);
            return new ServerError("Network topology is currently being modified. Please try again later.", "CONCURRENT_MODIFICATION");
        } catch (Exception e) {
            log.error("Unexpected error when adding device", e);
            return new ServerError("An unexpected error occurred: " + e.getMessage(), "INTERNAL_SERVER_ERROR");
        }
    }
}
