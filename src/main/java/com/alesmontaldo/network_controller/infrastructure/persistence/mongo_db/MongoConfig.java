package com.alesmontaldo.network_controller.infrastructure.persistence.mongo_db;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@EnableMongoRepositories(basePackages = {
        "com.alesmontaldo.network_controller.domain.club.persistence",
        "com.alesmontaldo.network_controller.domain.athlete.persistence",
        "com.alesmontaldo.network_controller.domain.activity.persistence"
})
public class MongoConfig {

    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener(LocalValidatorFactoryBean factory) {
        return new ValidatingMongoEventListener(factory);
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }
}
