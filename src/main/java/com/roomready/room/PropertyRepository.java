package com.roomready.room;

import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** The properties table. */
@Repository
public class PropertyRepository {

    private final JdbcClient jdbc;

    public PropertyRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** The database id of the property with this slug. */
    public Optional<Long> idOf(String slug) {
        return jdbc.sql("SELECT id FROM properties WHERE slug = ?").param(slug).query(Long.class).optional();
    }

    public long insert(String slug, String name) {
        jdbc.sql("INSERT INTO properties (slug, name) VALUES (?, ?)").params(slug, name).update();
        return idOf(slug).orElseThrow();
    }
}
