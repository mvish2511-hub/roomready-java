package com.roomready.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * The app's own settings, read once at start-up from application.yml. Spring's own settings - the database
 * URL, the host and the port - stay under spring.datasource and server. (The inspection policy is added
 * where it is first used.)
 *
 * @param seedDir the folder that holds rooms.csv and housekeepers.csv
 */
@ConfigurationProperties
public record RoomReadySettings(@DefaultValue("seed") String seedDir) {
}