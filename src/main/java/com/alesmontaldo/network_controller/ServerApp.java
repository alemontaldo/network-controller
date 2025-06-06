package com.alesmontaldo.network_controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServerApp {

	private static final Log log = LogFactory.getLog(ServerApp.class);

	public static void main(String[] args) {
		SpringApplication.run(ServerApp.class, args);
	}

}
