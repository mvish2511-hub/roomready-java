package com.roomready.room;

import java.util.List;

/** GET /properties/{property}/rooms: the rooms shown and, among them, how many are ready and how many dirty. */
public record BoardView(String property, int ready, int dirty, List<RoomView> rooms) {
}
