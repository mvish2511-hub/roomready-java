package com.roomready.room;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roomready.config.RoomReadySettings;

/**
 * Loads Harbour View, its 60 rooms (seed/rooms.csv) and its nine housekeepers (seed/housekeepers.csv) - once. A second
 * start finds the property and adds nothing.
 */
@Service
public class SeedService {

    static final String PROPERTY = "harbour-view";

    private final PropertyRepository properties;
    private final RoomRepository rooms;
    private final HousekeeperRepository housekeepers;
    private final RoomReadySettings settings;

    public SeedService(PropertyRepository properties, RoomRepository rooms, HousekeeperRepository housekeepers,
                       RoomReadySettings settings) {
        this.properties = properties;
        this.rooms = rooms;
        this.housekeepers = housekeepers;
        this.settings = settings;
    }

    @Transactional
    public void seed() {
        if (properties.idOf(PROPERTY).isPresent()) {
            return;
        }
        long property = properties.insert(PROPERTY, "Harbour View");
        for (String[] row : csv("rooms.csv")) {                 // no,floor,type
            rooms.insert(property, row[0], Integer.parseInt(row[1]), RoomType.valueOf(row[2]));
        }
        for (String[] row : csv("housekeepers.csv")) {          // id,name
            housekeepers.insert(property, row[0], row[1]);
        }
    }

    /** The rows of a seed file, header skipped, each split on commas. */
    private List<String[]> csv(String file) {
        try {
            return Files.readAllLines(Path.of(settings.seedDir(), file)).stream().skip(1)
                    .filter(line -> !line.isBlank()).map(line -> line.trim().split("\\s*,\\s*")).toList();
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read the seed file " + file, e);
        }
    }
}
