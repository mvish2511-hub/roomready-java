package com.roomready.error;

import java.util.Map;

/**
 * 409 /errors/step-skipped: the room is not in the state this action needs. `state` says where it is, needs the
 * step that must come first.
 */
public class StepSkipped extends DomainException {

    private final String state;
    private final String needs;

    public StepSkipped(String room, String state, String needs) {
        super(409, "/errors/step-skipped", "Step skipped",
                "Room " + room + " is " + state + "; it needs " + needs
                        + " first");
        this.state = state;
        this.needs = needs;
    }

    @Override
    public Map<String, Object> properties() {
        return Map.of("state", state, "needs", needs);
    }
}
