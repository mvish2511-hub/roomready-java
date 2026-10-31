package com.roomready.room;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.roomready.error.NoSuchProperty;
import com.roomready.error.NoSuchRoom;

/**
 * Reads the room board and individual rooms. The state is derived from the current stay and the latest
 * assignment, so the board always reflects the workflow.
 */
@Service
public class RoomService {

    private final PropertyRepository properties;
    private final RoomRepository rooms;
    private final HousekeeperRepository housekeepers;
    private final Clock clock;

    public RoomService(PropertyRepository properties, RoomRepository rooms,
                       HousekeeperRepository housekeepers, Clock clock) {
        this.properties = properties;
        this.rooms = rooms;
        this.housekeepers = housekeepers;
        this.clock = clock;
    }

    public BoardView board(String property, Integer floor, RoomState state, RoomType type) {
        long propertyId = propertyId(property);
        List<RoomView> views = rooms.all(propertyId).stream()
                .map(this::view)
                .filter(r -> floor == null || r.floor() == floor)
                .filter(r -> state == null || r.state() == state)
                .filter(r -> type == null || r.type() == type)
                .toList();

        List<RoomView> all = rooms.all(propertyId).stream().map(this::view).toList();
        int ready = (int) all.stream().filter(r -> r.state() == RoomState.ready).count();
        int dirty = (int) all.stream().filter(r -> r.state() == RoomState.dirty).count();

        return new BoardView(property, ready, dirty, views);
    }

    public RoomView room(String property, String no) {
        long propertyId = propertyId(property);
        return rooms.one(propertyId, no)
                .map(this::view)
                .orElseThrow(() -> new NoSuchRoom(property, no));
    }

    public HousekeepersView housekeepers(String property) {
        long propertyId = propertyId(property);
        List<HousekeeperView> views = housekeepers.all(propertyId).stream()
                .map(h -> new HousekeeperView(h.id(), h.name(), openAssignment(propertyId, h.id())))
                .toList();
        return new HousekeepersView(views);
    }

    private String openAssignment(long propertyId, String housekeeperId) {
        return rooms.all(propertyId).stream()
                .filter(r -> housekeeperId.equals(r.housekeeperId()))
                .filter(r -> view(r).assignedTo() != null)
                .map(RoomRepository.Row::no)
                .findFirst()
                .orElse(null);
    }

    /**
     * Derives the room's current state and, where relevant, while it is open, also gives who is on the room and for how many minutes it has been in this state. A failed
     * inspection closes the assignment: the room is dirty again until the next one.
     */
    private RoomView view(RoomRepository.Row row) {
        RoomState state = state(row);
        String assignedTo = null;
        Integer sinceMinutes = null;

        if (state == RoomState.assigned || state == RoomState.cleaning || state == RoomState.toinspect) {
            assignedTo = row.assignedTo();
            Instant since = switch (state) {
                case assigned -> row.assignedAt();
                case cleaning -> row.startedAt();
                case toinspect -> row.finishedAt();
                default -> null;
            };
            if (since != null) {
                sinceMinutes = (int) Duration.between(since, now()).toMinutes();
            }
        }

        return new RoomView(row.no(), row.floor(), row.type(), state, assignedTo, sinceMinutes);
    }

    private RoomState state(RoomRepository.Row row) {
        if (row.outOfOrder()) {
            return RoomState.out_of_order;
        }
        if (row.checkedOutAt() == null) {
            return RoomState.occupied;
        }
        if (row.assignedAt() == null || row.failed()) {
            return RoomState.dirty;
        }
        
        if (row.passed()) {
            return RoomState.ready;
        }
        
        if (row.startedAt() == null) {
            return RoomState.assigned;
        }
        if (row.finishedAt() == null) {
            return RoomState.cleaning;
        }
        return RoomState.toinspect;
    }

    private Instant now() {
        return clock.instant().truncatedTo(ChronoUnit.SECONDS);
    }

    private long propertyId(String property) {
        return properties.idOf(property).orElseThrow(() -> new NoSuchProperty(property));
    }
}
