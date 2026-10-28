package com.roomready.room;

/** A room's state, derived on every read from its rows - never stored. */
public enum RoomState {
    occupied, dirty, assigned, cleaning, toinspect, ready, out_of_order
}
