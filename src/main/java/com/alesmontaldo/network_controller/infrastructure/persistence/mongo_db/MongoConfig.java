package com.alesmontaldo.network_controller.infrastructure.persistence.mongo_db;

import com.alesmontaldo.network_controller.domain.device.persistance.mongo_db.converter.MacAddressToStringConverter;
import com.alesmontaldo.network_controller.domain.device.persistance.mongo_db.converter.StringToMacAddressConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Arrays;

@Configuration
@EnableMongoRepositories(basePackages = {
        "com.alesmontaldo.network_controller.domain.device.persistance"
})
@Profile("!in-memory")
public class MongoConfig {

    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener(LocalValidatorFactoryBean factory) {
        return new ValidatingMongoEventListener(factory);
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }
    
    /**
     * Registers custom converters for MongoDB to handle custom types.
     * This allows MongoDB to convert between domain types and database representations.
     */
    @Bean
    public MongoCustomConversions mongoCustomConversions(
            StringToMacAddressConverter stringToMacAddressConverter,
            MacAddressToStringConverter macAddressToStringConverter) {
        return new MongoCustomConversions(Arrays.asList(
                stringToMacAddressConverter,
                macAddressToStringConverter
        ));
    }
}
