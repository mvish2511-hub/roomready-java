package com.roomready.room;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** The rooms table. */
@Repository
public class RoomRepository {

    /** One rooms row. */
    public record Row(String no, int floor, RoomType type, boolean outOfOrder) {
    }

    private static final String SELECT = "SELECT no, floor, type, out_of_order FROM rooms WHERE property_id = ?";

    private final JdbcClient jdbc;

    public RoomRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Every room of a property, by number. */
    public List<Row> all(long propertyId) {
        return jdbc.sql(SELECT + " ORDER BY no").param(propertyId).query(Row.class).list();
    }

    public Optional<Row> one(long propertyId, String no) {
        return jdbc.sql(SELECT + " AND no = ?").params(propertyId, no).query(Row.class).optional();
    }

    public void insert(long propertyId, String no, int floor, RoomType type) {
        jdbc.sql("INSERT INTO rooms (property_id, no, floor, type, out_of_order) VALUES (?, ?, ?, ?, FALSE)")
                .params(propertyId, no, floor, type.name()).update();
    }
}
