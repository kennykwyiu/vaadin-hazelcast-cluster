package com.example.vaadincluster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.session.hazelcast.config.annotation.web.http.EnableHazelcastHttpSession;

/**
 * Main Spring Boot application class for Vaadin Hazelcast Cluster demo.
 * 
 * This application demonstrates:
 * - Vaadin 24 UI framework
 * - Spring Boot 3.4.5 backend
 * - Hazelcast distributed session management
 * - Embedded Tomcat clustering
 */
@SpringBootApplication
@EnableHazelcastHttpSession(maxInactiveIntervalInSeconds = 1800) // 30 minutes session timeout
public class VaadinHazelcastClusterApplication {

    public static void main(String[] args) {
        SpringApplication.run(VaadinHazelcastClusterApplication.class, args);
    }
}

