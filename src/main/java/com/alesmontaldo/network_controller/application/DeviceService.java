package com.alesmontaldo.network_controller.application;

import static com.alesmontaldo.network_controller.codegen.types.DeviceType.*;

import com.alesmontaldo.network_controller.codegen.types.*;
import com.alesmontaldo.network_controller.domain.activity.Activity;
import com.alesmontaldo.network_controller.domain.athlete.Athlete;
import com.alesmontaldo.network_controller.domain.club.Club;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import com.alesmontaldo.network_controller.domain.device.persistance.DeviceRepository;
import jakarta.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {

    private static final Log log = LogFactory.getLog(DeviceService.class);

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public Device getDeviceByMac(MacAddress mac) {
        log.info("Fetching device with mac: " + mac);
        return deviceRepository.findById(mac).orElse(null);
    }

    public Device addDevice(MacAddress mac, MacAddress uplinkMac, DeviceType deviceType) {
        log.info("Trying to add new device with mac:" + mac + ", uplinkMac: " + uplinkMac + " and deviceType: " + deviceType);

        if (Objects.equals(mac, uplinkMac)) {
            // this would open the door to circular connections and it's quite important
            throw new ValidationException("Device MAC cannot be the same as its uplink MAC");
        }

        Device newDevice = switch (deviceType) {
            case GATEWAY -> new Gateway(mac, uplinkMac, GATEWAY, new ArrayList<>());
            case SWITCH -> new Switch(mac, uplinkMac, SWITCH, new ArrayList<>());
            case ACCESS_POINT -> new AccessPoint(mac, uplinkMac, ACCESS_POINT, new ArrayList<>());
        };

        return deviceRepository.save(newDevice);
    }

    public Device getSubtree(MacAddress rootMac) {
        return deviceRepository.fetchSubtree(rootMac).orElse(null);
    }

    //Extra feature
    public void deleteDevice(MacAddress mac) {
        log.info("Deleting device with mac: " + mac);
        deviceRepository.deleteById(mac);
    }
}
