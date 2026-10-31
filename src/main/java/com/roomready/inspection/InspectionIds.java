package com.roomready.inspection;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roomready.assignment.AssignmentRepository;
import com.roomready.error.NoSuchHousekeeper;
import com.roomready.error.SelfInspection;
import com.roomready.error.StepSkipped;
import com.roomready.room.HousekeeperRepository;
import com.roomready.room.PropertyRepository;
import com.roomready.room.RoomService;
import com.roomready.room.RoomState;
import com.roomready.room.StayRepository;

/**
 * Inspections. Only a room waiting for inspection can be inspected, by a housekeeper who did not clean it. A failed
 * inspection closes the assignment and the room is dirty again; the next assignment is a new row. A passed one
 * closes it with the room ready and stamps the margin before the next guest - all in one transaction.
 */
@Service
public class InspectionIds {

    private final PropertyRepository properties;
    private final RoomService rooms;
    private final HousekeeperRepository housekeepers;
    private final AssignmentRepository assignments;
    private final InspectionRepository inspections;
    private final ReadinessRepository readiness;
    private final StayRepository stays;
    private final Clock clock;

    public InspectionIds(
            PropertyRepository properties,
            RoomService rooms,
            HousekeeperRepository housekeepers,
            AssignmentRepository assignments,
            InspectionRepository inspections,
            ReadinessRepository readiness,
            StayRepository stays,
            Clock clock) {

        this.properties = properties;
        this.rooms = rooms;
        this.housekeepers = housekeepers;
        this.assignments = assignments;
        this.inspections = inspections;
        this.readiness = readiness;
        this.stays = stays;
        this.clock = clock;
    }

    public static String format(long id) {
        return String.format("IN%03d", id);
    }

    /**
     * Checks in order: the room exists, it is waiting for inspection, the inspector exists, and did not clean it. The
     * inspection row and, on a pass, the readiness stamp are one transaction: one failure undoes both.
     */
    @Transactional
    public InspectionResponse inspect(String property, String room, String inspector, Result result) {
        long propertyId = properties.idOf(property).orElseThrow(); // the one property is seeded at start-up

        RoomState state = rooms.room(property, room).state();
        if (state != RoomState.toinspect) {
            throw new StepSkipped(room, state.name(), state.needs());
        }

        HousekeeperRepository.Row who = housekeepers.one(propertyId, inspector)
                .orElseThrow(() -> new NoSuchHousekeeper(inspector));

        AssignmentRepository.Latest clean = assignments.latest(propertyId, room).orElseThrow();

        if (clean.housekeeperId().equals(who.id())) {
            throw new SelfInspection(room, clean.cleanedBy());
        }

        Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        long id = inspections.insert(propertyId, room, clean.id(), who.id(), result, now);

        if (result == Result.fail) {
            return new InspectionResponse(InspectionIds.format(id), room, clean.cleanedBy(), who.name(), result,
                    rooms.room(property, room).state(), null, null, null);
        }

        Instant arrivalAt = stays.current(propertyId, room)
                .map(StayRepository.Row::arrivalAt)
                .orElse(null);
        Integer margin = arrivalAt == null ? null : (int) Duration.between(now, arrivalAt).toMinutes();

        readiness.insert(propertyId, room, now, arrivalAt, margin);

        return new InspectionResponse(InspectionIds.format(id), room, clean.cleanedBy(), who.name(), result,
                rooms.room(property, room).state(), now, arrivalAt, margin);
    }
}
