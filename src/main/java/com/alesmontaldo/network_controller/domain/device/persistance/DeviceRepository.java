package com.alesmontaldo.network_controller.domain.device.persistance;

import com.alesmontaldo.network_controller.codegen.types.Device;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DeviceRepository {

    private final DeviceMongoRepository deviceMongoRepository;
    private final DeviceMapper deviceMapper;

    @Autowired
    public DeviceRepository(DeviceMongoRepository deviceMongoRepository,
                            DeviceMapper deviceMapper) {
        this.deviceMongoRepository = deviceMongoRepository;
        this.deviceMapper = deviceMapper;
    }

    public Optional<Device> getDeviceById(String id) {
        Optional<DeviceDocument> deviceDocument = deviceMongoRepository.findById(id);
        return deviceDocument.map(deviceMapper::toDevice);
    }
}
