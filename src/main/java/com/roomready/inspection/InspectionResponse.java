package com.roomready.inspection;

import com.roomready.room.RoomState;

/** POST /inspections 201: who cleaned, who inspected, the result and the room's state now. */
public record InspectionResponse(String inspectionId, String room, String cleanedBy, String inspectedBy, Result result,
                                 RoomState state) {
}
