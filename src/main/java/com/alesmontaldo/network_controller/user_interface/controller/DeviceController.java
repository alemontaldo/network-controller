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
    public Object subtree(@Argument MacAddress macAddress) {
        Device device = deviceService.getSubtree(macAddress);
        if (device == null) {
            return null;
        }
        
        //return convertDeviceToMap(device);
        Map<String, Object> result = new HashMap<>();
        result.put("subtree", device);
        return result;
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
    
//    /**
//     * Recursively converts a Device object to a Map structure suitable for JSON serialization.
//     * This ensures the complete hierarchy is included without GraphQL field selection limitations.
//     */
//    private Map<String, Object> convertDeviceToMap(Device device) {
//        if (device == null) {
//            return null;
//        }
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("mac", device.getMacAddress().getValue());
//        result.put("deviceType", device.getDeviceType().toString());
//
//        if (device.getUplinkMacAddress() != null) {
//            result.put("uplinkMac", device.getUplinkMacAddress().getValue());
//        } else {
//            result.put("uplinkMac", null);
//        }
//
//        List<Map<String, Object>> children = new ArrayList<>();
//        if (device.getDownlinkDevices() != null) {
//            for (Device child : device.getDownlinkDevices()) {
//                children.add(convertDeviceToMap(child));
//            }
//        }
//        result.put("downlinkDevices", children);
//
//        return result;
//    }
}
