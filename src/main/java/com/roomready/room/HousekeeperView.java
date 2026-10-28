package com.roomready.room;

import com.fasterxml.jackson.annotation.JsonInclude;

/** One housekeeper and the room of their open assignment, if they have one. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HousekeeperView(String id, String name, String openAssignment) {
}
