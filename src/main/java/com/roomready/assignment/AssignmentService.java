package com.roomready.assignment;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roomready.error.AlreadyAssigned;
import com.roomready.error.NoSuchAssignment;
import com.roomready.error.NoSuchHousekeeper;
import com.roomready.error.StepSkipped;
import com.roomready.room.EventLog;
import com.roomready.room.HousekeeperRepository;
import com.roomready.room.PropertyRepository;
import com.roomready.room.RoomService;
import com.roomready.room.RoomState;
import com.roomready.room.RoomView;
import com.roomready.room.StayRepository;

/**
 * Who cleans a room, and the clean's stamps. Each step needs the room in one state (dirty to assign, assigned to
 * start, cleaning to finish); anything else is step-skipped. Every time comes from the injected clock, and each step
 * writes its event in the same transaction.
 */
@Service
public class AssignmentService {

    private final PropertyRepository properties;
    private final RoomService rooms;
    private final HousekeeperRepository housekeepers;
    private final StayRepository stays;
    private final AssignmentRepository assignments;
    private final EventLog events;
    private final Clock clock;

    public AssignmentService(
            PropertyRepository properties, RoomService rooms, HousekeeperRepository housekeepers,
            StayRepository stays, AssignmentRepository assignments, EventLog events, Clock clock) {
        this.properties = properties;
        this.rooms = rooms;
        this.housekeepers = housekeepers;
        this.stays = stays;
        this.assignments = assignments;
        this.events = events;
        this.clock = clock;
    }

    /**
     * The room must exist, then the housekeeper; a room somebody is already on is already-assigned, and
     * otherwise only a dirty room can be assigned.
     */
    @Transactional
    public AssignResponse assign(String property, String room, String housekeeper) {
        long propertyId = propertyId(property);
        RoomView view = rooms.room(property, room);
        HousekeeperRepository.Row who = housekeepers.one(propertyId, housekeeper)
                .orElseThrow(() -> new NoSuchHousekeeper(housekeeper));
        if (view.assignedTo() != null) {
            throw new AlreadyAssigned(room, view.assignedTo());
        }

        need(room, view.state(), RoomState.dirty);
        long stayId = stays.latest(propertyId, room).orElseThrow().id();
        Instant now = now();
        long id = assignments.insert(propertyId, room, stayId, who.id(), now);
        events.append(
                propertyId, room, stayId, id, "assigned", now, EventLog.SUPERVISOR,
                Map.of("housekeeper", who.id()));
        return new AssignResponse(AssignmentIds.format(id), room, who.name(), now);
    }

    @Transactional
    public StartResponse start(String property, String assignmentId) {
        AssignmentRepository.Row a = assignment(property, assignmentId);
        need(a.room(), rooms.room(property, a.room()).state(), RoomState.assigned);
        Instant now = now();
        assignments.start(a.id(), now);
        events.append(
                propertyId(property), a.room(), a.stayId(), a.id(), "started", now, a.housekeeperId(), Map.of());
        return new StartResponse(assignmentId, now);
    }

    /**
     * The clean's length, finished - started in whole minutes, is stamped here once and never recomputed.
     */
    @Transactional
    public FinishResponse finish(String property, String assignmentId) {
        AssignmentRepository.Row a = assignment(property, assignmentId);
        need(a.room(), rooms.room(property, a.room()).state(), RoomState.cleaning);
        Instant now = now();
        int cleanMinutes = (int) Duration.between(a.startedAt(), now).toMinutes();
        assignments.finish(a.id(), now, cleanMinutes);
        events.append(
                propertyId(property), a.room(), a.stayId(), a.id(), "finished", now, a.housekeeperId(),
                Map.of("cleanMinutes", cleanMinutes));
        return new FinishResponse(assignmentId, now, cleanMinutes);
    }

    private static void need(String room, RoomState state, RoomState needed) {
        if (state != needed) {
            throw new StepSkipped(room, state.name(), state.needs());
        }
    }

    private AssignmentRepository.Row assignment(String property, String assignmentId) {
        long propertyId = propertyId(property);
        return AssignmentIds.parse(assignmentId).flatMap(id -> assignments.find(propertyId, id))
                .orElseThrow(() -> new NoSuchAssignment(assignmentId));
    }

    private Instant now() {
        return clock.instant().truncatedTo(ChronoUnit.SECONDS);
    }

    private long propertyId(String property) {
        return properties.idOf(property).orElseThrow(); // the one property is seeded at start-up
    }
}
