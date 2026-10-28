package com.roomready.room;

import com.fasterxml.jackson.annotation.JsonInclude;

/** One room on the board; who is on it and for how long appear once a housekeeper is. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RoomView(String no, int floor, RoomType type, RoomState state, String assignedTo,
                       Integer sinceMinutes) {
}
