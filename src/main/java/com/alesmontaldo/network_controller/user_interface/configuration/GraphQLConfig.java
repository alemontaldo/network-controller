package com.alesmontaldo.network_controller.user_interface.configuration;

import com.alesmontaldo.network_controller.user_interface.scalar.MacAddressScalar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

/**
 * Configuration class for GraphQL setup.
 * Registers custom scalars and other GraphQL configurations.
 */
@Configuration
public class GraphQLConfig {

    /**
     * Configures the RuntimeWiring to include custom scalars.
     * 
     * @return RuntimeWiringConfigurer with custom scalar registrations
     */
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(MacAddressScalar.MAC_ADDRESS);
    }
}
