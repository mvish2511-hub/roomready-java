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

@Service
public class InspectionService {

    private final PropertyRepository properties;
    private final RoomService rooms;
    private final HousekeeperRepository housekeepers;
    private final AssignmentRepository assignments;
    private final InspectionRepository inspections;
    private final ReadinessRepository readiness;
    private final StayRepository stays;
    private final Clock clock;

    public InspectionService(
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
    
    @Transactional
    public InspectionResponse inspect(
            String property, String room, String inspector, Result result) {

        long propertyId = properties.idOf(property).orElseThrow();

        RoomState state = rooms.room(property, room).state();
        if (state != RoomState.toinspect) {
            throw new StepSkipped(room, state.name(), state.needs());
        }

        HousekeeperRepository.Row who = housekeepers.one(propertyId, inspector)
                .orElseThrow(() -> new NoSuchHousekeeper(inspector));

        AssignmentRepository.Latest clean =
                assignments.latest(propertyId, room).orElseThrow();

        if (clean.housekeeperId().equals(who.id())) {
            throw new SelfInspection(room, clean.cleanedBy());
        }

        Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);

        long id = inspections.insert(
                propertyId, room, clean.id(), who.id(), result, now);

        if (result == Result.fail) {
            return new InspectionResponse(
                    InspectionIds.format(id),
                    room,
                    clean.cleanedBy(),
                    who.name(),
                    result,
                    rooms.room(property, room).state(),
                    null,
                    null,
                    null);
        }

        Instant arrivalAt = stays.latest(propertyId, room)
        .map(StayRepository.Row::arrivalAt)
        .orElse(null);

        Integer margin = arrivalAt == null
                ? null
                : (int) Duration.between(now, arrivalAt).toMinutes();

        readiness.insert(propertyId, room, now, arrivalAt, margin);

        return new InspectionResponse(
                InspectionIds.format(id),
                room,
                clean.cleanedBy(),
                who.name(),
                result,
                rooms.room(property, room).state(),
                now,
                arrivalAt,
                margin);
    }
}