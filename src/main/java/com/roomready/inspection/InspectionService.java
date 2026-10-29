package com.roomready.inspection;

import java.time.Clock;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.roomready.assignment.AssignmentRepository;
import com.roomready.error.NoSuchHousekeeper;
import com.roomready.error.SelfInspection;
import com.roomready.error.StepSkipped;
import com.roomready.room.HousekeeperRepository;
import com.roomready.room.PropertyRepository;
import com.roomready.room.RoomService;
import com.roomready.room.RoomState;

/**
 * Inspections. Only a room waiting for inspection can be inspected, by a housekeeper who did not clean it. A failed
 * inspection closes the assignment and the room is dirty again; the next assignment is a new row.
 */
@Service
public class InspectionService {

    private final PropertyRepository properties;
    private final RoomService rooms;
    private final HousekeeperRepository housekeepers;
    private final AssignmentRepository assignments;
    private final InspectionRepository inspections;
    private final Clock clock;

    public InspectionService(PropertyRepository properties, RoomService rooms,HousekeeperRepository housekeepers,
    AssignmentRepository assignments,                             InspectionRepository inspections, Clock clock) {
        this.properties = properties;
        this.rooms = rooms;
        this.housekeepers = housekeepers;
        this.assignments = assignments;
        this.inspections = inspections;
        this.clock = clock;
    }

  /** Checks in order: the room exists, it is waiting for inspection, the inspector exists, and did not clean it.*/
   public InspectionResponse inspect(String property, String room, String inspector, Result result) {
        long propertyId = properties.idOf(property).orElseThrow();   // the one property is seeded at start-up
        RoomState state = rooms.room(property, room).state();
        if (state != RoomState.toinspect) {
            throw new StepSkipped(room, state.name(), state.needs());
        }
        HousekeeperRepository.Row who = housekeepers.one(propertyId, inspector)
                .orElseThrow(() -> new NoSuchHousekeeper(inspector));
        AssignmentRepository.Latest clean = assignments.latest(propertyId, room).orElseThrow();
        if (clean.housekeeperId().equals(who.id())) {
            throw new SelfInspection(room, clean.cleanedBy());
        }
        long id = inspections.insert(propertyId, room, clean.id(), who.id(), result,
                clock.instant().truncatedTo(ChronoUnit.SECONDS));
        return new InspectionResponse(InspectionIds.format(id), room, clean.cleanedBy(), who.name(), result,
                rooms.room(property, room).state());
    }
}
