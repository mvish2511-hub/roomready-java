package com.roomready.room;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * One line of a room's day: when, what, and that event's own facts (the housekeeper, the clean's minutes, the
 * inspection's result and margin), which appear next to `at` and `kind` in the JSON.
 */
@JsonPropertyOrder({"at", "kind"})
public record EventView(Instant at, String kind, @JsonAnyGetter Map<String, Object> facts) {
}
