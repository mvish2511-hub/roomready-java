package com.roomready.error;

/** 404 /errors/no-such-property. */
public class NoSuchProperty extends DomainException {

    public NoSuchProperty(String property) {
        super(404, "/errors/no-such-property", "No such property", "There is no property " + property);
    }
}
