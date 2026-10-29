package com.roomready.inspection;

/** An inspection's public id - IN077 - made from its database id, which is never shown itself. */
public final class InspectionIds {

    private InspectionIds() {
    }

    public static String format(long id) {
        return String.format("IN%03d", id);
    }
}
