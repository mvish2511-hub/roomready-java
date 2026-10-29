package com.roomready.error;

/** 409 /errors/self-inspection: the inspector cleaned this room; somebody else must inspect it. */
public class SelfInspection extends DomainException {

    public SelfInspection(String room, String inspector) {
        super(409, "/errors/self-inspection", "Self-inspection", inspector + " cleaned room " + room
                + "; somebody else must inspect it");
    }
}
