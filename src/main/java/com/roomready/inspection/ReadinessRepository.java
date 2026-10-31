package com.roomready.inspection;

import java.time.Instant;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** The room_readiness table: when a room was ready, when its next guest was due, and the margin between. */
@Repository
public class ReadinessRepository {

    private final JdbcClient jdbc;

    public ReadinessRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(long propertyId, String room, Instant readyAt, Instant arrivalAt, Integer marginMinutes) {
        jdbc.sql("""
                        INSERT INTO room_readiness (room_id, ready_at, arrival_at, margin_minutes)
                        SELECT id, ?, ?, ? FROM rooms WHERE property_id = ? AND no = ?""")
                .params(readyAt, arrivalAt, marginMinutes, propertyId, room).update();
    }
}
