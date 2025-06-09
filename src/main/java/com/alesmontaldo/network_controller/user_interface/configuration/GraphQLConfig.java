package com.alesmontaldo.network_controller.user_interface.configuration;

import com.alesmontaldo.network_controller.codegen.types.*;
import com.alesmontaldo.network_controller.domain.device.MacAddress;
import com.alesmontaldo.network_controller.user_interface.scalar.MacAddressScalar;
import graphql.scalars.ExtendedScalars;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

/**
 * Configuration class for GraphQL setup.
 * Registers custom scalars, type resolvers, and other GraphQL configurations.
 */
@Configuration
@ImportRuntimeHints(GraphQLConfig.GraphQLRuntimeHints.class)
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
                        if (result instanceof Gateway) {
                            return env.getSchema().getObjectType("Gateway");
                        } else if (result instanceof Switch) {
                            return env.getSchema().getObjectType("Switch");
                        } else if (result instanceof AccessPoint) {
                            return env.getSchema().getObjectType("AccessPoint");
                        } else if (result instanceof ValidationError) {
                            return env.getSchema().getObjectType("ValidationError");
                        } else if (result instanceof ServerError) {
                            return env.getSchema().getObjectType("ServerError");
                        }
                        return null;
                    }));

            wiringBuilder.type("GetDeviceResult", typeConfig -> typeConfig
                    .typeResolver(env -> {
                        Object result = env.getObject();
                        if (result instanceof DeviceResultView) {
                            return env.getSchema().getObjectType("DeviceResultView");
                        } else if (result instanceof ValidationError) {
                            return env.getSchema().getObjectType("ValidationError");
                        } else if (result instanceof ServerError) {
                            return env.getSchema().getObjectType("ServerError");
                        }
                        return null;
                    }));

            wiringBuilder.type("DeviceTopologyResult", typeConfig -> typeConfig
                    .typeResolver(env -> {
                        Object result = env.getObject();
                        if (result instanceof JsonResult) {
                            return env.getSchema().getObjectType("JsonResult");
                        } else if (result instanceof ValidationError) {
                            return env.getSchema().getObjectType("ValidationError");
                        } else if (result instanceof ServerError) {
                            return env.getSchema().getObjectType("ServerError");
                        }
                        return null;
                    }));
        };
    }

    // Hints for types are required for native java compilation
    static class GraphQLRuntimeHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.reflection().registerType(MacAddress.class, MemberCategory.values());
            hints.reflection().registerType(Device.class, MemberCategory.values());
            hints.reflection().registerType(Gateway.class, MemberCategory.values());
            hints.reflection().registerType(AccessPoint.class, MemberCategory.values());
            hints.reflection().registerType(Switch.class, MemberCategory.values());
            hints.reflection().registerType(ValidationError.class, MemberCategory.values());
            hints.reflection().registerType(ServerError.class, MemberCategory.values());
            hints.reflection().registerType(JsonResult.class, MemberCategory.values());
            hints.reflection().registerType(GetDeviceResult.class, MemberCategory.values());
            hints.reflection().registerType(DeviceResultView.class, MemberCategory.values());
            hints.reflection().registerType(AddDeviceResult.class, MemberCategory.values());
            hints.reflection().registerType(DeviceTopologyResult.class, MemberCategory.values());
        }
    }
}
