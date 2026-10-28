package com.roomready.error;

/** 404 /errors/no-such-room. */
public class NoSuchRoom extends DomainException {

    public NoSuchRoom(String property, String no) {
        super(404, "/errors/no-such-room", "No such room", "There is no room " + no + " in " + property);
    }
}
