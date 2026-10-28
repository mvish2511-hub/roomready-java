package com.roomready.checkout;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Service;

import com.roomready.error.InvalidRequest;
import com.roomready.error.NoSuchRoom;
import com.roomready.error.RoomNotOccupied;
import com.roomready.room.PropertyRepository;
import com.roomready.room.RoomRepository;
import com.roomready.room.RoomState;
import com.roomready.room.StayIds;
import com.roomready.room.StayRepository;

/** The guest leaves: the stay is stamped and the room's derived state becomes dirty. */
@Service
public class CheckoutService {

    private final PropertyRepository properties;
    private final RoomRepository rooms;
    private final StayRepository stays;
    private final Clock clock;

    public CheckoutService(PropertyRepository properties, RoomRepository rooms, StayRepository stays, Clock clock) {
        this.properties = properties;
        this.rooms = rooms;
        this.stays = stays;
        this.clock = clock;
    }

    /**
     * The room must exist; the times are checked against the clock and the stay (each a validation error naming its
     * field); a room whose latest stay is already checked out has no guest to check out.
     */
    public CheckoutResponse checkout(String property, String room, Instant at, Instant arrivalAt) {
        long propertyId = properties.idOf(property).orElseThrow();   // the one property is seeded at start-up
        rooms.one(propertyId, room).orElseThrow(() -> new NoSuchRoom(property, room));
        if (at.isAfter(clock.instant())) {
            throw new InvalidRequest("at", "must not be in the future");
        }
        if (arrivalAt != null && arrivalAt.isBefore(at)) {
            throw new InvalidRequest("arrivalAt", "must not be before the checkout");
        }
        StayRepository.Row stay = stays.current(propertyId, room).filter(s -> s.checkedOutAt() == null)
                .orElseThrow(() -> new RoomNotOccupied(room));
        if (at.isBefore(stay.checkedInAt())) {
            throw new InvalidRequest("at", "must not be before check-in at " + stay.checkedInAt());
        }
        stays.checkOut(stay.id(), at, arrivalAt);
        return new CheckoutResponse(StayIds.format(stay.id()), room, at, RoomState.dirty);
    }
}
