package com.roomready.room;

/** A room's state, derived on every read from its rows - never stored. */
public enum RoomState {
    occupied, dirty, assigned, cleaning, toinspect, ready, out_of_order;

    /** The step that must come next from this state: what a step-skipped refusal says the room needs. */
    public String needs() {
        return switch (this) {
            case occupied -> "checkout";
            case dirty -> "assign";
            case assigned -> "start";
            case cleaning -> "finish";
            case toinspect -> "inspect";
            case ready -> "checkin";
            case out_of_order -> throw new IllegalStateException("no step leads out of order");
        };
    }
}
