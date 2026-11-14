package com.roomready.error;

/** The example error bodies the API docs show for each refusal - the exact shape a client will receive. */
public final class ProblemExamples {

    public static final String PROBLEM = "application/problem+json";

    public static final String VALIDATION_QUERY = """
            {"type":"/errors/validation","title":"Invalid request","status":400,"detail":"The request is not valid",\
            "instance":"/properties/harbour-view/rooms","errors":[{"field":"state",\
            "message":"'painting' is not an allowed value"}]}""";
    public static final String VALIDATION_CHECKOUT = """
            {"type":"/errors/validation","title":"Invalid request","status":400,"detail":"The request is not valid",\
            "instance":"/checkouts","errors":[{"field":"at","message":"must not be null"}]}""";
    public static final String VALIDATION_ASSIGN = """
            {"type":"/errors/validation","title":"Invalid request","status":400,"detail":"The request is not valid",\
            "instance":"/assignments","errors":[{"field":"housekeeper","message":"must not be blank"}]}""";
    public static final String VALIDATION_INSPECT = """
            {"type":"/errors/validation","title":"Invalid request","status":400,"detail":"The request is not valid",\
            "instance":"/inspections","errors":[{"field":"result","message":"this value cannot be read"}]}""";
    public static final String NO_SUCH_PROPERTY = """
            {"type":"/errors/no-such-property","title":"No such property","status":404,\
            "detail":"There is no property sea-view","instance":"/properties/sea-view/rooms"}""";
    public static final String NO_SUCH_ROOM = """
            {"type":"/errors/no-such-room","title":"No such room","status":404,\
            "detail":"There is no room 999 in harbour-view","instance":"/properties/harbour-view/rooms/999"}""";
    public static final String NO_SUCH_ROOM_HISTORY = """
            {"type":"/errors/no-such-room","title":"No such room","status":404,\
            "detail":"There is no room 999 in harbour-view","instance":"/properties/harbour-view/rooms/999/history"}""";
    public static final String NO_SUCH_ROOM_CHECKOUT = """
            {"type":"/errors/no-such-room","title":"No such room","status":404,\
            "detail":"There is no room 999 in harbour-view","instance":"/checkouts"}""";
    public static final String NO_SUCH_ROOM_ASSIGN = """
            {"type":"/errors/no-such-room","title":"No such room","status":404,\
            "detail":"There is no room 999 in harbour-view","instance":"/assignments"}""";
    public static final String NO_SUCH_ROOM_INSPECT = """
            {"type":"/errors/no-such-room","title":"No such room","status":404,\
            "detail":"There is no room 999 in harbour-view","instance":"/inspections"}""";
    public static final String ROOM_NOT_OCCUPIED = """
            {"type":"/errors/room-not-occupied","title":"Room not occupied","status":400,\
            "detail":"Room 214 has no guest to check out","instance":"/checkouts"}""";
    public static final String NO_SUCH_HOUSEKEEPER_ASSIGN = """
            {"type":"/errors/no-such-housekeeper","title":"No such housekeeper","status":404,\
            "detail":"There is no housekeeper HK99","instance":"/assignments"}""";
    public static final String NO_SUCH_HOUSEKEEPER_INSPECT = """
            {"type":"/errors/no-such-housekeeper","title":"No such housekeeper","status":404,\
            "detail":"There is no housekeeper HK99","instance":"/inspections"}""";
    public static final String STEP_SKIPPED_ASSIGN = """
            {"type":"/errors/step-skipped","title":"Step skipped","status":409,\
            "detail":"Room 214 is occupied; it needs checkout first","instance":"/assignments","state":"occupied",\
            "needs":"checkout"}""";
    public static final String ALREADY_ASSIGNED = """
            {"type":"/errors/already-assigned","title":"Already assigned","status":409,\
            "detail":"Room 214 is already assigned to Sunita","instance":"/assignments","assignedTo":"Sunita"}""";
    public static final String STEP_SKIPPED_START = """
            {"type":"/errors/step-skipped","title":"Step skipped","status":409,\
            "detail":"Room 214 is cleaning; it needs finish first","instance":"/assignments/AS118/start",\
            "state":"cleaning","needs":"finish"}""";
    public static final String STEP_SKIPPED_FINISH = """
            {"type":"/errors/step-skipped","title":"Step skipped","status":409,\
            "detail":"Room 214 is assigned; it needs start first","instance":"/assignments/AS118/finish",\
            "state":"assigned","needs":"start"}""";
    public static final String NO_SUCH_ASSIGNMENT_START = """
            {"type":"/errors/no-such-assignment","title":"No such assignment","status":404,\
            "detail":"There is no assignment AS999","instance":"/assignments/AS999/start"}""";
    public static final String NO_SUCH_ASSIGNMENT_FINISH = """
            {"type":"/errors/no-such-assignment","title":"No such assignment","status":404,\
            "detail":"There is no assignment AS999","instance":"/assignments/AS999/finish"}""";
    public static final String STEP_SKIPPED_INSPECT = """
            {"type":"/errors/step-skipped","title":"Step skipped","status":409,\
            "detail":"Room 214 is cleaning; it needs finish first","instance":"/inspections","state":"cleaning",\
            "needs":"finish"}""";
    public static final String SELF_INSPECTION = """
            {"type":"/errors/self-inspection","title":"Self-inspection","status":409,\
            "detail":"Sunita cleaned room 214; somebody else must inspect it","instance":"/inspections"}""";

    private ProblemExamples() {
    }
}

