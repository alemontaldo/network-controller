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

    //TODO keep me?
    private DeviceMongoRepository deviceMongoRepository;

    @Autowired
    public void setActivityMongoMapper(DeviceMongoRepository deviceMongoRepository) {
        this.deviceMongoRepository = deviceMongoRepository;
    }

    /**
     * Maps a DeviceDocument (mongo type) to a Device (domain type)
     * This is a dispatcher method that will call the appropriate method based on the type
     */
    public Device toDevice(DeviceDocument document) {
        if (document == null) {
            return null;
        }
        
        return switch (document.getDeviceType()) {
            case "GATEWAY" -> toGateway((GatewayDocument) document);
            case "SWITCH" -> toSwitch((SwitchDocument) document);
            case "ACCESS_POINT" -> toAccessPoint((AccessPointDocument) document);
            default -> throw new IllegalArgumentException("Unknown device type: " + document.getDeviceType());
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
    
    @Mapping(target = "deviceType", ignore = true)
    public abstract GatewayDocument toGatewayDocument(Gateway gateway);
    
    @Mapping(target = "deviceType", ignore = true)
    public abstract SwitchDocument toSwitchDocument(Switch switchDevice);
    
    @Mapping(target = "deviceType", ignore = true)
    public abstract AccessPointDocument toAccessPointDocument(AccessPoint accessPoint);

    @AfterMapping
    protected void setGatewayDocumentDeviceType(@MappingTarget GatewayDocument document) {
        document.setDeviceType("GATEWAY");
    }
    
    @AfterMapping
    protected void setSwitchDocumentDeviceType(@MappingTarget SwitchDocument document) {
        document.setDeviceType("SWITCH");
    }
    
    @AfterMapping
    protected void setAccessPointDocumentDeviceType(@MappingTarget AccessPointDocument document) {
        document.setDeviceType("ACCESS_POINT");
    }
}
