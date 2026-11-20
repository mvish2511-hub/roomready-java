package com.roomready.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import com.roomready.room.RoomType;

/**
 * The app's own settings, read once at start-up: application.yml gives the defaults, and an environment
 * variable overrides each one. Spring's own settings - the database URL, the host and the port - stay under
 * spring.datasource and server. 
 
 * @param seedDir the folder that holds rooms.csv and housekeepers.csv
 * @param inspectionRequiredFor  the room types a finished clean must be inspected for; the others are ready
 *                              at once
 * @param stallAfterMinutes     minutes after which a clean in progress counts as stalled
 *                              (shown here)
 */
@ConfigurationProperties
public record RoomReadySettings(@DefaultValue("seed") String seedDir,
                              @DefaultValue("Standard,Deluxe,Suite") List<RoomType> inspectionRequiredFor,
                              @DefaultValue("60") int stallAfterMinutes ) {
}