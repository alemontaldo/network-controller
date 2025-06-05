package com.alesmontaldo.network_controller;

import com.alesmontaldo.network_controller.domain.club.Club;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ServerApp {

	private static final Log logger = LogFactory.getLog(ServerApp.class);

	public static void main(String[] args) {
		SpringApplication.run(ServerApp.class, args);
	}

	@Bean
	GraphQlSourceBuilderCustomizer graphQlSourceBuilderCustomizer() {
		return builder -> builder.inspectSchemaMappings(
				initializer -> initializer.classMapping("Club", Club.class),
				logger::debug);
	}

}
