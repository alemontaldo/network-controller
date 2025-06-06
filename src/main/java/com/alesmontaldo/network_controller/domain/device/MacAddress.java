package com.alesmontaldo.network_controller.domain.device;

import java.util.Objects;
import java.util.regex.Pattern;

public class MacAddress {
    private final String value;

    /**
     * Regular expression pattern for validating MAC addresses.
     * Explanation:
     * ^                 - Start of the string
     * ([0-9A-Fa-f]{2}  - Two hexadecimal characters (0-9, A-F, a-f)
     * [:-])            - Followed by either a colon or hyphen separator
     * {5}              - The above pattern repeats exactly 5 times
     * ([0-9A-Fa-f]{2}) - Followed by two more hexadecimal characters
     * $                - End of the string
     * 
     * This validates formats like:
     * - 01:23:45:67:89:AB
     * - 01-23-45-67-89-ab
     */
    private static final Pattern MAC_PATTERN =
            Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");

    public MacAddress(String macAddress) {
        if (macAddress == null || !MAC_PATTERN.matcher(macAddress).matches()) {
            throw new IllegalArgumentException("Invalid MAC address: " + macAddress);
        }
        this.value = macAddress.toUpperCase();
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MacAddress that = (MacAddress) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
