package com.alesmontaldo.network_controller.user_interface.controller;

import com.alesmontaldo.network_controller.application.ActivityService;
import com.alesmontaldo.network_controller.application.AthleteService;
import com.alesmontaldo.network_controller.application.DeviceService;
import com.alesmontaldo.network_controller.codegen.types.Device;
import com.alesmontaldo.network_controller.codegen.types.DeviceType;
import com.alesmontaldo.network_controller.domain.activity.Activity;
import com.alesmontaldo.network_controller.domain.athlete.Athlete;
import com.alesmontaldo.network_controller.domain.club.Club;
import com.alesmontaldo.network_controller.domain.comment.Comment;
import java.util.List;
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
    public Device deviceByMac(@Argument String mac) {
        return deviceService.getDeviceByMac(mac);
    }

    @MutationMapping
    public Device addDevice(@Argument("input") Map<String, Object> input) {
        String mac = (String) input.get("mac");
        String uplinkMac = (String) input.get("uplinkMac");
        DeviceType deviceType = DeviceType.valueOf((String) input.get("deviceType"));

        return deviceService.addDevice(mac, uplinkMac, deviceType);
    }

//    @SchemaMapping
//    public List<Activity> activities(Athlete athlete) {
//        return athleteService.getActivitiesForAthlete(athlete);
//    }
//
//    @BatchMapping
//    public Map<Activity, List<Comment>> comments(List<Activity> activities) {
//        return activityService.getCommentsForActivities(activities);
//    }
}
