package com.roomready.assignment;

import java.time.Instant;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 * The assignments table: who cleans a room after a stay, and the clean's stamps.
 */
@Repository
public class AssignmentRepository {

    /** One assignment and the number of its room. */
    public record Row(long id, String room, Instant startedAt) {
    }

    private final JdbcClient jdbc;

    public AssignmentRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * A new assignment of this room, for the stay that just ended, to this housekeeper; its database id.
     */
    public long insert(long propertyId, String room, long stayId, String housekeeperId, Instant assignedAt) {
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.sql("""
                        INSERT INTO assignments
                        (property_id, room_id, stay_id, housekeeper_id, assigned_at)
                        SELECT ?, id, ?, ?, ? FROM rooms WHERE property_id = ? AND no = ?""")
                .params(propertyId, stayId, housekeeperId, assignedAt, propertyId, room)
                .update(key, "id");
        return key.getKeyAs(Long.class);
    }

    public Optional<Row> find(long propertyId, long id) {
        return jdbc.sql("""
                        SELECT a.id, r.no AS room, a.started_at
                        FROM assignments a
                        JOIN rooms r ON r.id = a.room_id
                        WHERE a.property_id = ? AND a.id = ?""")
                .params(propertyId, id).query(Row.class).optional();
    }

    public void start(long id, Instant at) {
        jdbc.sql("UPDATE assignments SET started_at = ? WHERE id = ?").params(at, id).update();
    }

    public void finish(long id, Instant at, int cleanMinutes) {
        jdbc.sql("UPDATE assignments SET finished_at = ?, clean_minutes = ? WHERE id = ?")
                .params(at, cleanMinutes, id)
                .update();
    }
}
