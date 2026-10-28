package com.roomready.checkout;

import java.time.Instant;

import com.roomready.room.RoomState;

/** POST /checkouts 201: the stay that ended and the room's state now. */
public record CheckoutResponse(String stayId, String room, Instant checkedOutAt, RoomState state) {
}