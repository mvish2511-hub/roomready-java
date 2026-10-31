package com.roomready.inspection;

import java.time.Instant;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** The inspections table. */
@Repository
public class InspectionRepository {

    private final JdbcClient jdbc;

    public InspectionRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** An inspection of this assignment's clean; its database id. */
    public long insert(long propertyId,
                       String room,
                       long assignmentId,
                       String inspectorId,
                       Result result,
                       Instant inspectedAt) {

        KeyHolder key = new GeneratedKeyHolder();

        jdbc.sql("""
                 INSERT INTO inspections
                 (property_id, room_id, assignment_id, inspector_id, result, inspected_at)
                 SELECT ?, id, ?, ?, ?, ?
                 FROM rooms
                 WHERE property_id = ? AND no = ?
                 """)
                .params(propertyId,
                        assignmentId,
                        inspectorId,
                        result.name(),
                        inspectedAt,
                        propertyId,
                        room)
                .update(key, "id");

        return key.getKeyAs(Long.class);
    }
}