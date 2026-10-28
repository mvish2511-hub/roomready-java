package com.roomready.room;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roomready.config.RoomReadySettings;

/**
 *  Loads Harbour View, its 60 rooms (seed/rooms.csv) and its nine housekeepers (seed/housekeepers.csv), and puts a
 * guest who checked in yesterday in every room - each once. A second start finds them and adds nothing.
 */
@Service
public class SeedService {

    static final String PROPERTY = "harbour-view";

    private final PropertyRepository properties;
    private final RoomRepository rooms;
    private final HousekeeperRepository housekeepers;
    private final StayRepository stays;
    private final RoomReadySettings settings;
    private final Clock clock;

     public SeedService(PropertyRepository properties,RoomRepository rooms, HousekeeperRepository housekeepers,
                       StayRepository stays, RoomReadySettings settings, Clock clock) {
        this.properties = properties;
        this.rooms = rooms;
        this.housekeepers = housekeepers;
        this.stays = stays;
        this.settings = settings;
        this.clock = clock;
    }

    @Transactional
    public void seed() {
        long property = properties.idOf(PROPERTY).orElseGet(this::property);
        if (stays.none(property)) {                             // the hotel is full when the app first starts
            stays.checkInEveryRoom(property,
                    clock.instant().truncatedTo(ChronoUnit.SECONDS).minus(Duration.ofDays(1)));
        }
    }

    private long property() {
        long property = properties.insert(PROPERTY, "Harbour View");
        for (String[] row : csv("rooms.csv")) {                 // no,floor,type
            rooms.insert(property, row[0], Integer.parseInt(row[1]), RoomType.valueOf(row[2]));
        }
        for (String[] row : csv("housekeepers.csv")) {          // id,name
            housekeepers.insert(property, row[0], row[1]);
        }
        return property;
    }

    /** The rows of a seed file, header skipped, each split on commas. */
    private List<String[]> csv(String file) {
        try {
            return Files.readAllLines(Path.of(settings.seedDir(), file)).stream().skip(1)
                    .filter(line -> !line.isBlank())
                    .map(line -> line.trim().split("\\s*,\\s*")).toList();
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read the seed file " + file, e);
        }
    }
}