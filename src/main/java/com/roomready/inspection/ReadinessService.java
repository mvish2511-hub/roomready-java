package com.roomready.inspection;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * The room is ready: when, the next guest's arrival and the margin between (negative when late, null when no arrival
 * is known) are stamped once - after a passed inspection, or at the finish of a clean no inspection is needed
 * for. The margin and the arrival also go into the facts of the event that records it.
 */
@Service
public class ReadinessService {

    private final ReadinessRepository readiness;

    public ReadinessService(ReadinessRepository readiness) {
        this.readiness = readiness;
    }

    public Integer stamp(
            long propertyId,
            String room,
            Instant now,
            Instant arrivalAt,
            Map<String, Object> facts) {

        Integer margin = arrivalAt == null ? null :
                (int) Duration.between(now, arrivalAt).toMinutes();

        readiness.insert(propertyId, room, now, arrivalAt, margin);

        if (arrivalAt != null) {
            facts.put("marginMinutes", margin);
            facts.put("arrivalAt", arrivalAt.toString());
        }

        return margin;
    }
}
