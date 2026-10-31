package com.roomready.room;

import java.time.Instant;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** The stays table. A room's current stay is its latest one; it is open while checked_out_at is null. */
@Repository
public class StayRepository {

        /** A room's latest stay, with the next guest's arrival when the desk typed it in. */
    public record Row(long id, Instant checkedInAt, Instant checkedOutAt, Instant arrivalAt) {
    }


    private final JdbcClient jdbc;

    public StayRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Row> current(long propertyId, String roomNo) {
        return jdbc.sql("""
                        SELECT s.id, s.checked_in_at, s.checked_out_at, s.arrival_at
                        FROM stays s JOIN rooms r ON r.id = s.room_id
                        WHERE s.property_id = ? AND r.no = ? AND s.checked_out_at IS NULL
                        ORDER BY s.id DESC LIMIT 1""")
                .params(propertyId, roomNo)
                .query(Row.class)
                .optional();
    }
    public Optional<Row> latest(long propertyId, String roomNo) {
    return jdbc.sql("""
            SELECT s.id, s.checked_in_at, s.checked_out_at, s.arrival_at
            FROM stays s
            JOIN rooms r ON r.id = s.room_id
            WHERE s.property_id = ? AND r.no = ?
            ORDER BY s.id DESC
            LIMIT 1
            """)
            .params(propertyId, roomNo)
            .query(Row.class)
            .optional();
}

public boolean none(long propertyId) {
        return jdbc.sql("SELECT count(*) FROM stays WHERE property_id = ?").param(propertyId).query(Long.class)
                .single() == 0;
    }

    /** A guest in every room of the property, checked in at this moment (the seed). */
    public void checkInEveryRoom(long propertyId, Instant checkedInAt) {
        jdbc.sql("""
                        INSERT INTO stays (property_id, room_id, guest_ref, checked_in_at)
                        SELECT property_id, id, 'BK-' || no || '-1', ? FROM rooms WHERE property_id = ?""")
                .params(checkedInAt, propertyId).update();
    }

    /** The guest leaves; the next guest's arrival, when the desk knows it, is kept for the readiness margin. */
    public void checkOut(long stayId, Instant at, Instant arrivalAt) {
        jdbc.sql("UPDATE stays SET checked_out_at = ?, arrival_at = ? WHERE id = ?").params(at, arrivalAt, stayId)
                .update();
    }
}
