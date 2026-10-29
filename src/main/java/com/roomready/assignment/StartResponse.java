package com.roomready.assignment;

import java.time.Instant;

/** POST /assignments/{assignmentId}/start 200: the room is cleaning. */
public record StartResponse(String assignmentId, Instant startedAt) {
}
