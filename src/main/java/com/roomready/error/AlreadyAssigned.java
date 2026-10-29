package com.roomready.error;

import java.util.Map;

/** 409 /errors/already-assigned: the room already has an open assignment; `assignedTo` names the housekeeper. */
public class AlreadyAssigned extends DomainException {

    private final String assignedTo;

    public AlreadyAssigned(String room, String assignedTo) {
        super(409, "/errors/already-assigned", "Already assigned", "Room " + room + " is already assigned to "
                + assignedTo);
        this.assignedTo = assignedTo;
    }

    @Override
    public Map<String, Object> properties() {
        return Map.of("assignedTo", assignedTo);
    }
}
