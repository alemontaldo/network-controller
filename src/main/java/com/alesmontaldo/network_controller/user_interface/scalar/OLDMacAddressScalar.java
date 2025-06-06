package com.alesmontaldo.network_controller.user_interface.scalar;

import com.alesmontaldo.network_controller.domain.device.MacAddress;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.springframework.stereotype.Component;

/**
 * Custom GraphQL scalar type for MAC addresses.
 * This scalar handles conversion between GraphQL string values and MacAddress domain objects.
 */
//TODO: remove me
@Component
public class OLDMacAddressScalar {
    
    private final GraphQLScalarType type;
    
    public OLDMacAddressScalar() {
        this.type = GraphQLScalarType.newScalar()
                .name("MacAddress")
                .description("A MAC address in the format XX:XX:XX:XX:XX:XX or XX-XX-XX-XX-XX-XX")
                .coercing(new Coercing<MacAddress, String>() {
                    @Override
                    public String serialize(Object dataFetcherResult) {
                        if (dataFetcherResult instanceof MacAddress) {
                            return ((MacAddress) dataFetcherResult).getValue();
                        } else if (dataFetcherResult instanceof String) {
                            // If it's already a string, validate it as a MAC address
                            try {
                                return new MacAddress((String) dataFetcherResult).getValue();
                            } catch (IllegalArgumentException e) {
                                throw new CoercingSerializeException("Invalid MAC address: " + dataFetcherResult);
                            }
                        }
                        throw new CoercingSerializeException("Expected a MacAddress object or string");
                    }

                    @Override
                    public MacAddress parseValue(Object input) {
                        if (input instanceof String) {
                            try {
                                return new MacAddress((String) input);
                            } catch (IllegalArgumentException e) {
                                throw new CoercingParseValueException("Invalid MAC address: " + input);
                            }
                        }
                        throw new CoercingParseValueException("Expected a string");
                    }

                    @Override
                    public MacAddress parseLiteral(Object input) {
                        if (input instanceof StringValue) {
                            try {
                                return new MacAddress(((StringValue) input).getValue());
                            } catch (IllegalArgumentException e) {
                                throw new CoercingParseLiteralException("Invalid MAC address: " + input);
                            }
                        }
                        throw new CoercingParseLiteralException("Expected a string");
                    }
                })
                .build();
    }
    
    public GraphQLScalarType getType() {
        return type;
    }
}
