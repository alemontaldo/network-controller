package com.alesmontaldo.network_controller.user_interface.controller;

import com.alesmontaldo.network_controller.application.DeviceService;
import com.alesmontaldo.network_controller.codegen.types.*;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import jakarta.validation.ValidationException;
import java.util.ConcurrentModificationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.graphql.data.method.annotation.*;
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
    public Object addDevice(@Argument("input") Map<String, Object> input) {
        try {
            MacAddress mac = (MacAddress) input.get("mac");
            MacAddress uplinkMac = (MacAddress) input.get("uplinkMac");
            DeviceType deviceType = DeviceType.valueOf((String) input.get("deviceType"));

            return deviceService.addDevice(mac, uplinkMac, deviceType);
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
