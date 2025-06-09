package com.alesmontaldo.network_controller.domain.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MacAddressTest {

    @Test
    void integrationTest_SerializeAndDeserialize_Success() throws IOException {
        // Given
        final ObjectMapper objectMapper = new ObjectMapper();
        MacAddress original = new MacAddress("AA:BB:CC:DD:EE:FF");

        // When
        String json = objectMapper.writeValueAsString(original);
        MacAddress deserialized = objectMapper.readValue(json, MacAddress.class);

        assertThat(json).isEqualTo("\"AA:BB:CC:DD:EE:FF\"");
        assertThat(deserialized.getValue())
                .isNotNull()
                .isEqualTo(original.getValue());
    }

    @Test
    void constructor_ValidMacAddressWithColons_Success() {
        // Given
        String validMac = "AA:BB:CC:DD:EE:FF";
        
        // When
        MacAddress macAddress = new MacAddress(validMac);
        
        // Then
        assertThat(macAddress.getValue()).isEqualTo(validMac);
    }
    
    @Test
    void constructor_ValidMacAddressWithHyphens_Success() {
        // Given
        String validMac = "AA-BB-CC-DD-EE-FF";
        
        // When
        MacAddress macAddress = new MacAddress(validMac);
        
        // Then
        assertThat(macAddress.getValue()).isEqualTo(validMac);
    }
    
    @Test
    void constructor_LowercaseMacAddress_ConvertedToUppercase() {
        // Given
        String lowercaseMac = "aa:bb:cc:dd:ee:ff";
        String expectedMac = "AA:BB:CC:DD:EE:FF";
        
        // When
        MacAddress macAddress = new MacAddress(lowercaseMac);
        
        // Then
        assertThat(macAddress.getValue()).isEqualTo(expectedMac);
    }
    
    @Test
    void constructor_MixedCaseMacAddress_ConvertedToUppercase() {
        // Given
        String mixedCaseMac = "Aa:Bb:Cc:Dd:Ee:Ff";
        String expectedMac = "AA:BB:CC:DD:EE:FF";
        
        // When
        MacAddress macAddress = new MacAddress(mixedCaseMac);
        
        // Then
        assertThat(macAddress.getValue()).isEqualTo(expectedMac);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
            "G0:00:00:00:00:00",  // Invalid hex character
            "00:00:00:00:00",     // Too short
            "00:00:00:00:00:00:00", // Too long
            "00-00-00-00-00",     // Too short with hyphens
            "0000.0000.0000",     // Invalid format (Cisco format)
            "00:00:00:00:00:0",   // Incomplete octet
            "",                   // Empty string
            "AABBCCDDEEFF"        // No separators
    })
    void constructor_InvalidMacAddress_ThrowsIllegalArgumentException(String invalidMac) {
        // When/Then
        assertThatThrownBy(() -> new MacAddress(invalidMac))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid MAC address");
    }
    
    @Test
    void constructor_NullMacAddress_ThrowsIllegalArgumentException() {
        // When/Then
        assertThatThrownBy(() -> new MacAddress(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid MAC address");
    }
    
    @Test
    void toString_ReturnsValue() {
        // Given
        String mac = "AA:BB:CC:DD:EE:FF";
        MacAddress macAddress = new MacAddress(mac);
        
        // When
        String result = macAddress.toString();
        
        // Then
        assertThat(result).isEqualTo(mac);
    }
    
    @Test
    void equals_EqualMacAddresses_ReturnsTrue() {
        // Given
        MacAddress macAddress1 = new MacAddress("AA:BB:CC:DD:EE:FF");
        MacAddress macAddress2 = new MacAddress("AA:BB:CC:DD:EE:FF");
        
        // When/Then
        assertThat(macAddress1).isEqualTo(macAddress2);
    }
    
    @Test
    void equals_DifferentMacAddresses_ReturnsFalse() {
        // Given
        MacAddress macAddress1 = new MacAddress("AA:BB:CC:DD:EE:FF");
        MacAddress macAddress2 = new MacAddress("11:22:33:44:55:66");
        
        // When/Then
        assertThat(macAddress1).isNotEqualTo(macAddress2);
    }
    
    @Test
    void equals_Null_ReturnsFalse() {
        // Given
        MacAddress macAddress = new MacAddress("AA:BB:CC:DD:EE:FF");
        
        // When/Then
        assertThat(macAddress).isNotNull();
        assertThat(macAddress).isNotEqualTo(null);
    }
    
    @Test
    void hashCode_SameMacAddresses_ReturnsSameHashCode() {
        // Given
        MacAddress macAddress1 = new MacAddress("AA:BB:CC:DD:EE:FF");
        MacAddress macAddress2 = new MacAddress("AA:BB:CC:DD:EE:FF");
        
        // When/Then
        assertThat(macAddress1).hasSameHashCodeAs(macAddress2);
    }
}
