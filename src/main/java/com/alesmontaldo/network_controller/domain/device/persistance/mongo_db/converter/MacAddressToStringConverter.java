package com.alesmontaldo.network_controller.domain.device.persistance.mongo_db.converter;

import com.alesmontaldo.network_controller.domain.device.MacAddress;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

/**
 * Converter to transform MacAddress objects to String values for MongoDB storage.
 * Used when writing data to the database.
 */
@Component
@WritingConverter
public class MacAddressToStringConverter implements Converter<MacAddress, String> {
    @Override
    public String convert(MacAddress source) {
        if (source == null) {
            return null;
        }
        return source.getValue();
    }
}
