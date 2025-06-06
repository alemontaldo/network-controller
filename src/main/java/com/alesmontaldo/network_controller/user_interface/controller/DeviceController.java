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

        return deviceService.addDevice(mac, uplinkMac, deviceType);
    }

    //Extra feature
    @MutationMapping
    public DeleteDeviceResponse deleteDevice(@Argument("mac") MacAddress mac) {
        deviceService.deleteDevice(mac);
        // only happy path here
        return new DeleteDeviceResponse(true, "Device successfully deleted", mac);
    }
}
