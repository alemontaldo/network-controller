package com.alesmontaldo.network_controller.domain.device;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class MacAddressJsonConverter {

    /**
     * Serializes MacAddress objects to simple string values
     */
    public static class MacAddressSerializer extends JsonSerializer<MacAddress> {
        @Override
        public void serialize(MacAddress macAddress, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (macAddress == null) {
                jsonGenerator.writeNull();
            } else {
                jsonGenerator.writeString(macAddress.getValue());
            }
        }
    }

    /**
     * Deserializes string values to MacAddress objects
     */
    public static class MacAddressDeserializer extends JsonDeserializer<MacAddress> {
        @Override
        public MacAddress deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            String value = jsonParser.getValueAsString();
            if (value == null || value.isEmpty()) {
                return null;
            }
            return new MacAddress(value);
        }
    }
}
