package com.roomready.error;

/** 404 /errors/no-such-assignment. */
public class NoSuchAssignment extends DomainException {

    public NoSuchAssignment(String id) {
        super(404, "/errors/no-such-assignment", "No such assignment", "There is no assignment " + id);
    }
}
