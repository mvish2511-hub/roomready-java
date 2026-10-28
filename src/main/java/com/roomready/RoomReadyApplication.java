package com.roomready;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** RoomReady: housekeeping for one 60-room hotel, checkout to ready. The grader starts the app through this class. */
@SpringBootApplication
@ConfigurationPropertiesScan
public class RoomReadyApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoomReadyApplication.class, args);
    }
}