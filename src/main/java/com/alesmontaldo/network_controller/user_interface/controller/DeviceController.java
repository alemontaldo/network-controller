package com.alesmontaldo.network_controller.user_interface.controller;

import com.alesmontaldo.network_controller.application.DeviceService;
import com.alesmontaldo.network_controller.codegen.types.*;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import jakarta.validation.ValidationException;
import java.util.ConcurrentModificationException;
import java.util.List;
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
    public GetDeviceResult getDevice(@Argument MacAddress macAddress) {
        try {
            log.info("Finding device with mac address: " + macAddress);
            Device device = deviceService.getDeviceByMac(macAddress);
            return new DeviceResultView(device.getMacAddress(), device.getDeviceType());
        } catch (ValidationException e) {
            log.warn("Validation error when getting device: " + e.getMessage());
            return new ValidationError(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when getting device for mac address:" + macAddress);
            return new ServerError("An unexpected error occurred: " + e.getMessage(), "INTERNAL_SERVER_ERROR");
        }
    }

    @QueryMapping
    public List<? extends GetDeviceResult> allDevicesSorted() {
        try {
            return deviceService.getAllDevicesSorted();
        } catch (Exception e) {
            log.error("Unexpected error when getting all devices sorted", e);
            return List.of(new ServerError("An unexpected error occurred: " + e.getMessage(), "INTERNAL_SERVER_ERROR"));
        }
    }

    @MutationMapping
    public AddDeviceResult addDevice(@Argument("input") DeviceInput deviceInput) {
        try {
            return (AddDeviceResult) deviceService.addDevice(deviceInput.getMacAddress(), deviceInput.getUplinkMacAddress(), deviceInput.getDeviceType());
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

    @QueryMapping
    public DeviceTopologyResult fullTopology() {
        try {
            return new JsonResult(deviceService.getFullTopology());
        } catch (Exception e) {
            log.error("Error retrieving full topology", e);
            return new ServerError("Failed to retrieve network topology: " + e.getMessage(), "INTERNAL_SERVER_ERROR");
        }
    }

    @QueryMapping
    public DeviceTopologyResult deviceTopology(@Argument MacAddress macAddress) {
        try {
            Device device = deviceService.getSubtree(macAddress);
            return new JsonResult(deviceService.buildSimplifiedTopology(device));
        } catch (ValidationException e) {
            log.warn("Validation error when getting device topology: " + e.getMessage());
            return new ValidationError(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when getting device topology for mac address:" + macAddress);
            return new ServerError("An unexpected error occurred: " + e.getMessage(), "INTERNAL_SERVER_ERROR");
        }
    }
}
