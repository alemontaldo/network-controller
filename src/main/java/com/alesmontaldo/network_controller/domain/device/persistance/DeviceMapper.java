package com.alesmontaldo.network_controller.domain.device.persistance;

import com.alesmontaldo.network_controller.codegen.types.AccessPoint;
import com.alesmontaldo.network_controller.codegen.types.Device;
import com.alesmontaldo.network_controller.codegen.types.Gateway;
import com.alesmontaldo.network_controller.codegen.types.Switch;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public abstract class DeviceMapper {

    /**
     * Maps a DeviceDocument (mongo type) to a Device (domain type)
     * This is a dispatcher method that will call the appropriate method based on the type
     */
    public Device toDevice(DeviceDocument document) {
        if (document == null) {
            return null;
        }
        
        return switch (document.getDeviceType()) {
            case GATEWAY -> toGateway((GatewayDocument) document);
            case SWITCH -> toSwitch((SwitchDocument) document);
            case ACCESS_POINT -> toAccessPoint((AccessPointDocument) document);
        };
    }

    /**
     * Maps a Device (domain type) object to a DeviceDocument (mongo type)
     * This is a dispatcher method that will call the appropriate method based on the runtime type
     */
    public DeviceDocument toDocument(Device activity) {
        return switch (activity) {
            case null -> null;
            case Gateway gateway -> toGatewayDocument(gateway);
            case Switch switchDevice -> toSwitchDocument(switchDevice);
            case AccessPoint accessPoint -> toAccessPointDocument(accessPoint);
            default -> throw new IllegalArgumentException("Unknown activity type: " + activity.getClass().getName());
        };
    }
    
    // Specific mappers for each concrete type
    public abstract Gateway toGateway(GatewayDocument document);

    public abstract Switch toSwitch(SwitchDocument document);

    public abstract AccessPoint toAccessPoint(AccessPointDocument document);
    
    public abstract GatewayDocument toGatewayDocument(Gateway gateway);
    
    public abstract SwitchDocument toSwitchDocument(Switch switchDevice);
    
    public abstract AccessPointDocument toAccessPointDocument(AccessPoint accessPoint);
}
