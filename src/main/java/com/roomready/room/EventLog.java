package com.roomready.room;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Append-only event log.
 * Events can be inserted and read, but never updated or deleted.
 */
@Repository
public class EventLog {

    public static final String FRONT_DESK = "front-desk";
    public static final String SUPERVISOR = "supervisor";

    private record Row(Instant at, String kind, String data) {
    }

    private final JdbcClient jdbc;
    private final ObjectMapper json;

    public EventLog(JdbcClient jdbc, ObjectMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    /**
     * Inserts one event for a room's stay.
     */
    public void append(
            long propertyId,
            String room,
            long stayId,
            Long assignmentId,
            String kind,
            Instant at,
            String actorId,
            Map<String, Object> facts) {

        int inserted = jdbc.sql("""
                INSERT INTO events (
                    property_id, room_id, stay_id,
                    assignment_id, kind, at, actor_id, data
                )
                SELECT ?, id, ?, ?, ?, ?, ?, ?
                FROM rooms
                WHERE property_id = ? AND no = ?
                """)
                .params(
                        propertyId,
                        stayId,
                        assignmentId,
                        kind,
                        at,
                        actorId,
                        write(facts),
                        propertyId,
                        room)
                .update();

        if (inserted != 1) {
            throw new IllegalStateException(
                    "Failed to append event: " + kind
                    + " for room " + room);
        }
    }

    /**
     * Returns events belonging to one stay,
     * ordered by timestamp and event ID.
     */
    public List<EventView> forStay(
            long propertyId, String room, long stayId) {

        return jdbc.sql("""
                SELECT e.at, e.kind, e.data
                FROM events e
                JOIN rooms r ON r.id = e.room_id
                WHERE e.property_id = ?
                  AND r.no = ?
                  AND e.stay_id = ?
                ORDER BY e.at ASC, e.id ASC
                """)
                .params(propertyId, room, stayId)
                .query((rs, rowNum) -> new Row(
                        rs.getTimestamp("at").toInstant(),
                        rs.getString("kind"),
                        rs.getString("data")))
                .list()
                .stream()
                .map(r -> new EventView(
                        r.at(),
                        r.kind(),
                        read(r.data())))
                .toList();
    }

    private String write(Map<String, Object> facts) {
        try {
            return json.writeValueAsString(facts);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Event facts must be JSON", e);
        }
    }

    private Map<String, Object> read(String data) {
        try {
            return json.readValue(
                    data,
                    new TypeReference<Map<String, Object>>() {
                    });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "An events row contains invalid JSON", e);
        }
    }
}
