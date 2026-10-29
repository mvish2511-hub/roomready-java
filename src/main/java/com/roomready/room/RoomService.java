package com.roomready.room;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.roomready.error.NoSuchProperty;
import com.roomready.error.NoSuchRoom;

/**
 * The board: every room with its state, derived from its rows on every read; and the housekeepers.
 */
@Service
public class RoomService {

    private final PropertyRepository properties;
    private final RoomRepository rooms;
    private final HousekeeperRepository housekeepers;
    private final Clock clock;

    public RoomService(PropertyRepository properties, RoomRepository rooms, HousekeeperRepository housekeepers,
                       Clock clock) {
        this.properties = properties;
        this.rooms = rooms;
        this.housekeepers = housekeepers;
        this.clock = clock;
    }

    public BoardView board(String property, Integer floor, RoomState state, RoomType type) {
        List<RoomView> shown = rooms.all(propertyId(property)).stream()
                .map(this::view)
                .filter(r -> floor == null || r.floor() == floor)
                .filter(r -> state == null || r.state() == state)
                .filter(r -> type == null || r.type() == type)
                .toList();
        return new BoardView(property, count(shown, RoomState.ready), count(shown, RoomState.dirty), shown);
    }

    public RoomView room(String property, String no) {
        return view(rooms.one(propertyId(property), no).orElseThrow(() -> new NoSuchRoom(property, no)));
    }

    private long propertyId(String property) {
        return properties.idOf(property).orElseThrow(() -> new NoSuchProperty(property));
    }

    /**
     * Derived on every read. Out of order overrides all; a room is occupied while its latest stay has no checkout,
     * dirty once it has, and then assigned, cleaning or to inspect by the stamps of that stay's assignment - which,
     * while it is open, also gives who is on the room and for how many minutes it has been in this state.
     */
     
    private RoomView view(RoomRepository.Row row) {
        RoomState state = state(row);
        Instant since = switch (state) {
            case assigned -> row.assignedAt();
            case cleaning -> row.startedAt();
            case toinspect -> row.finishedAt();
            default -> null;
        };
        if (since == null) {
            return new RoomView(row.no(), row.floor(), row.type(), state, null, null);
        }
        return new RoomView(row.no(), row.floor(), row.type(), state, row.assignedTo(),
                (int) Duration.between(since, clock.instant()).toMinutes());
    }

    private static RoomState state(RoomRepository.Row row) {
        if (row.outOfOrder()) {
            return RoomState.out_of_order;
        }
        if (row.checkedOutAt() == null) {
            return RoomState.occupied;
        }
        if (row.assignedAt() == null) {
            return RoomState.dirty;
        }
        if (row.startedAt() == null) {
            return RoomState.assigned;
        }
        return row.finishedAt() == null ? RoomState.cleaning : RoomState.toinspect;
    }

    private static int count(List<RoomView> shown, RoomState state) {
        return (int) shown.stream().filter(r -> r.state() == state).count();
    }

    /**
     * Every housekeeper, with the room of the open assignment they hold (a room assigned, cleaning or to inspect).
     */
    public HousekeepersView housekeepers(String property) {
        long propertyId = propertyId(property);
        Map<String, String> open = rooms.all(propertyId).stream()
                .filter(r -> r.housekeeperId() != null && view(r).assignedTo() != null)
                .collect(Collectors.toMap(RoomRepository.Row::housekeeperId, RoomRepository.Row::no));
        return new HousekeepersView(housekeepers.all(propertyId).stream()
                .map(h -> new HousekeeperView(h.id(), h.name(), open.get(h.id()))).toList());
    }
}
