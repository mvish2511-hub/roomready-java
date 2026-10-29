package com.roomready.assignment;

import java.time.Instant;

/** POST /assignments/{assignmentId}/finish 200: the clean's length, stamped once. */
public record FinishResponse(String assignmentId, Instant finishedAt, int cleanMinutes) {
}
