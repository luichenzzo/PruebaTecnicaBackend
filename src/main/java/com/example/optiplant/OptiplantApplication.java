package com.example.optiplant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Optiplant backend application.
 */
@SpringBootApplication
public class OptiplantApplication {

    /**
     * Starts the Spring Boot runtime.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(OptiplantApplication.class, args);
    }

}
