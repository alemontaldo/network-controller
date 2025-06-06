package com.alesmontaldo.network_controller.user_interface.scalar;

import com.alesmontaldo.network_controller.domain.device.MacAddress;
import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.*;
import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MacAddressScalar {

    private static final Log logger = LogFactory.getLog(MacAddressScalar.class);

    public static final GraphQLScalarType MAC_ADDRESS = GraphQLScalarType.newScalar()
            .name("MacAddress")
            .description("A custom mac address type")
            .coercing(new Coercing() {
                @Override
                public Object serialize(Object dataFetcherResult, GraphQLContext graphQLContext, Locale locale) {
                    return serializeMacAddress(dataFetcherResult);
                }

                @Override
                public Object parseValue(Object input, GraphQLContext graphQLContext, Locale locale) {
                    return parseMacAddressFromVariable(input);
                }

                @Override
                public Object parseLiteral(Value input, CoercedVariables variables, GraphQLContext graphQLContext, Locale locale) {
                    return parseMacAddressFromAstLiteral(input);
                }
            })
            .build();

    private static Object serializeMacAddress(Object dataFetcherResult) {

        String serializedMacAddress = String.valueOf(dataFetcherResult);
        try {
            return new MacAddress(serializedMacAddress).toString();
        } catch (Exception e) {
            logger.error("Error while serializing a mac address with exception: ", e);
            throw new CoercingSerializeException("Unable to serialize " + serializedMacAddress + " as a mac address");
        }
    }

    private static Object parseMacAddressFromVariable(Object input) {
        if (!(input instanceof String)) {
            throw new CoercingParseValueException("Unable to parse variable value " + input + " as a mac address");
        }

        String serializedMacAddress = input.toString();
        try {
            return new MacAddress(serializedMacAddress);
        } catch (Exception e) {
            logger.error("Error while deserializing a mac address with exception: ", e);
            throw new CoercingParseValueException("Unable to deserializing " + serializedMacAddress + " as a mac address");
        }
    }

    private static Object parseMacAddressFromAstLiteral(Object input) {
        if (!(input instanceof StringValue)) {
            throw new CoercingParseLiteralException("Value is not a mac address : '" + input + "'");
        }

        String serializedMacAddress = ((StringValue) input).getValue();
        try {
            return new MacAddress(serializedMacAddress);
        } catch (Exception e) {
            logger.error("Error while deserializing a mac address with exception: ", e);
            throw new CoercingParseLiteralException("Value is not a mac address : '" + input + "'");
        }
    }
}
