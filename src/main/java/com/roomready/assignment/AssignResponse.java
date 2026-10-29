package com.roomready.assignment;

import java.time.Instant;

/** POST /assignments 201. */
public record AssignResponse(String assignmentId, String room, String assignedTo, Instant assignedAt) {
}
