package com.roomready.room;

/** A stay's public id - ST0412 - made from its database id, which is never shown itself. */
public final class StayIds {

    private StayIds() {
    }

    public static String format(long id) {
        return String.format("ST%04d", id);
    }
}