package com.alesmontaldo.network_controller.domain.device.persistance.mongo_db.converter;

import com.alesmontaldo.network_controller.domain.device.MacAddress;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

/**
 * Converter to transform String values from MongoDB to MacAddress objects.
 * Used when reading data from the database.
 */
@Component
@ReadingConverter
public class StringToMacAddressConverter implements Converter<String, MacAddress> {
    @Override
    public MacAddress convert(String source) {
        if (source == null) {
            return null;
        }
        return new MacAddress(source);
    }
}
