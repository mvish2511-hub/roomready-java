package com.roomready.error;

/** 400 /errors/room-not-occupied: a valid body, but the room has no guest to check out. */
public class RoomNotOccupied extends DomainException {

    public RoomNotOccupied(String no) {
        super(400, "/errors/room-not-occupied", "Room not occupied", "Room " + no + " has no guest to check out");
    }
}
