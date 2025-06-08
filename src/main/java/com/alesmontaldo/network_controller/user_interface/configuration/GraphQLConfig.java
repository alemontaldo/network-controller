package com.alesmontaldo.network_controller.user_interface.configuration;

import com.alesmontaldo.network_controller.codegen.types.Device;
import com.alesmontaldo.network_controller.codegen.types.ServerError;
import com.alesmontaldo.network_controller.codegen.types.ValidationError;
import com.alesmontaldo.network_controller.user_interface.scalar.MacAddressScalar;
import graphql.scalars.ExtendedScalars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

/**
 * Configuration class for GraphQL setup.
 * Registers custom scalars, type resolvers, and other GraphQL configurations.
 */
@Configuration
public class GraphQLConfig {

    /**
     * Configures the RuntimeWiring to include custom scalars and type resolvers.
     * 
     * @return RuntimeWiringConfigurer with custom registrations
     */
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> {
            // Register custom scalars
            wiringBuilder.scalar(MacAddressScalar.MAC_ADDRESS);
            wiringBuilder.scalar(ExtendedScalars.Json);
            
            // Register type resolvers for the union types
            wiringBuilder.type("AddDeviceResult", typeConfig -> typeConfig
                    .typeResolver(env -> {
                        Object result = env.getObject();
                        if (result instanceof Device) {
                            return env.getSchema().getObjectType("Device");
                        } else if (result instanceof ValidationError) {
                            return env.getSchema().getObjectType("ValidationError");
                        } else if (result instanceof ServerError) {
                            return env.getSchema().getObjectType("ServerError");
                        }
                        return null;
                    }));
        };
    }
}
