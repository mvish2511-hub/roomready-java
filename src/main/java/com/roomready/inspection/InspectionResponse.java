package com.roomready.inspection;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.roomready.room.RoomState;

/**
 * POST /inspections 201: who cleaned, who inspected, the result and the room's state now; on a pass also when
 * the room was ready, when its next guest is due and the margin between - those two are absent when no arrival is
 * known.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record InspectionResponse(String inspectionId, String room, String cleanedBy, String inspectedBy, Result result,
                                 RoomState state, Instant readyAt, Instant arrivalAt, Integer marginMinutes) {
}
