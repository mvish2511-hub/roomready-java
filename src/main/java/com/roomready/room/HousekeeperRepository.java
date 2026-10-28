package com.roomready.room;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** The housekeepers table. Its id is the housekeeper's own id from the seed file (HK03), never a database number. */
@Repository
public class HousekeeperRepository {

    /** One housekeepers row. */
    public record Row(String id, String name) {
    }

    private final JdbcClient jdbc;

    public HousekeeperRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Row> all(long propertyId) {
        return jdbc.sql("SELECT id, name FROM housekeepers WHERE property_id = ? ORDER BY id").param(propertyId)
                .query(Row.class).list();
    }

    public void insert(long propertyId, String id, String name) {
        jdbc.sql("INSERT INTO housekeepers (id, property_id, name) VALUES (?, ?, ?)")                .params(id, propertyId, name)
                .update();
    }
}
