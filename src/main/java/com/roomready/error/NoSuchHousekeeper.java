package com.roomready.error;

/** 404 /errors/no-such-housekeeper. */
public class NoSuchHousekeeper extends DomainException {

    public NoSuchHousekeeper(String id) {
        super(404, "/errors/no-such-housekeeper", "No such housekeeper", "There is no housekeeper " + id);
    }
}
