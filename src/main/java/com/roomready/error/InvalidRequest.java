package com.roomready.error;

import java.util.List;
import java.util.Map;

/**
 * 400 /errors/validation for a rule the service checks against the clock or the stored rows (a checkout time in the
 * future or before check-in, an arrival before the checkout): the same shape as a body that fails Bean Validation,
 * with `errors[]` naming the field.
 */
public class InvalidRequest extends DomainException {

    private final String field;
    private final String reason;

    public InvalidRequest(String field, String reason) {
        super(400, "/errors/validation", "Invalid request", "The request is not valid");
        this.field = field;
        this.reason = reason;
    }

    @Override
    public Map<String, Object> properties() {
        return Map.of("errors", List.of(Map.of("field", field, "message", reason)));
    }
}