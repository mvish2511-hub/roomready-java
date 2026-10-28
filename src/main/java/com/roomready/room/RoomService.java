package com.roomready.room;

import java.util.List;

import org.springframework.stereotype.Service;

import com.roomready.error.NoSuchProperty;
import com.roomready.error.NoSuchRoom;

/** The board: every room with its state, derived from its rows on every read; and the housekeepers. */
@Service
public class RoomService {

    private final PropertyRepository properties;
    private final RoomRepository rooms;
    private final HousekeeperRepository housekeepers;

    public RoomService(PropertyRepository properties, RoomRepository rooms, HousekeeperRepository housekeepers) {
        this.properties = properties;
        this.rooms = rooms;
        this.housekeepers = housekeepers;
    }

    /** The board, optionally one floor, one state and/or one type; `ready` and `dirty` count the rooms shown. */
    public BoardView board(String property, Integer floor, RoomState state, RoomType type) {
        List<RoomView> shown = rooms.all(propertyId(property)).stream()
                .map(RoomService::view)
                .filter(r -> floor == null || r.floor() == floor)
                .filter(r -> state == null || r.state() == state)
                .filter(r -> type == null || r.type() == type)
                .toList();
return new BoardView(property, count(shown, RoomState.ready), count(shown, RoomState.dirty), shown);
    }

    public RoomView room(String property, String no) {
        return view(rooms.one(propertyId(property), no).orElseThrow(() -> new NoSuchRoom(property, no)));
    }

    public HousekeepersView housekeepers(String property) {
        return new HousekeepersView(housekeepers.all(propertyId(property)).stream()
                        .map(h -> new HousekeeperView(h.id(), h.name(), null)).toList());
    }

    private long propertyId(String property) {
        return properties.idOf(property).orElseThrow(() -> new NoSuchProperty(property));
    }

    /**
     * Derived on every read. Out of order overrides all; a room is occupied while its latest stay has no checkout and
     * dirty once it has.
     */
    private static RoomView view(RoomRepository.Row row) {
        RoomState state = row.outOfOrder() ? RoomState.out_of_order : row.checkedOutAt() == null ? RoomState.occupied : RoomState.dirty;
        return new RoomView(row.no(), row.floor(), row.type(), state, null, null);
    }

    private static int count(List<RoomView> shown, RoomState state) {
        return (int) shown.stream().filter(r -> r.state() == state).count();
    }
}