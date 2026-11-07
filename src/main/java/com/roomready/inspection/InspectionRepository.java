package com.roomready.inspection;

import java.time.Instant;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class InspectionRepository {

    private final JdbcClient jdbc;

    public InspectionRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public long insert(
            long propertyId,
            String room,
            long assignmentId,
            String inspectorId,
            Result result,
            Instant inspectedAt) {

        KeyHolder key = new GeneratedKeyHolder();

        int inserted = jdbc.sql("""
                INSERT INTO inspections
                (property_id, room_id, assignment_id, inspector_id, result, inspected_at)
                SELECT ?, id, ?, ?, ?, ?
                FROM rooms
                WHERE property_id = ? AND no = ?
                """)
                .params(
                        propertyId,
                        assignmentId,
                        inspectorId,
                        result.name(),
                        inspectedAt,
                        propertyId,
                        room)
                .update(key, "id");

        if (inserted != 1 || key.getKey() == null) {
            throw new IllegalStateException(
                    "Inspection could not be inserted for room " + room);
        }

        return ((Number) key.getKey()).longValue();
    }
}
