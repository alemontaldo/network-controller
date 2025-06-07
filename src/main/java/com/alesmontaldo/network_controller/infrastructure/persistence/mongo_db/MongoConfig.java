package com.alesmontaldo.network_controller.infrastructure.persistence.mongo_db;

import com.alesmontaldo.network_controller.domain.device.persistance.converter.MacAddressToStringConverter;
import com.alesmontaldo.network_controller.domain.device.persistance.converter.StringToMacAddressConverter;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableMongoRepositories(basePackages = {
        "com.alesmontaldo.network_controller.domain.device.persistance"
})
public class MongoConfig {

    private final MongoTemplate mongoTemplate;

    public MongoConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    //TODO consider remove
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
    
    /**
     * Initialize MongoDB indices after application startup
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initIndicesAfterStartup() {
        // Create TTL index for topology locks to automatically expire
        MongoCollection<Document> lockCollection = 
            mongoTemplate.getCollection("topology_locks");
        
        // Create a TTL index that automatically removes expired locks
        lockCollection.createIndex(
            Indexes.ascending("expiresAt"), 
            new IndexOptions().expireAfter(0L, TimeUnit.SECONDS)
        );
    }
}
