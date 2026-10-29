package com.roomready.room;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** The rooms table. */
@Repository
public class RoomRepository {

    /** 
     * One rooms row, with the checkout of its latest stay (null while the guest is in) and that stay's latest
     * assignment: who has it, its three stamps (all null when there is none) and whether an inspection failed it.
     */
    public record Row(String no, int floor, RoomType type, boolean outOfOrder, Instant checkedOutAt,
                      String housekeeperId, String assignedTo, Instant assignedAt, Instant startedAt,
                      Instant finishedAt, boolean failed)  {
    }

    private static final String SELECT = """
            SELECT r.no, r.floor, r.type, r.out_of_order, s.checked_out_at, a.housekeeper_id, h.name AS assigned_to,
                   a.assigned_at, a.started_at, a.finished_at,
                   EXISTS (SELECT 1 FROM inspections i WHERE i.assignment_id = a.id AND i.result = 'fail') AS failed
            FROM rooms r
            LEFT JOIN stays s ON s.id = (SELECT MAX(id) FROM stays WHERE room_id = r.id)
            LEFT JOIN assignments a ON a.id = (SELECT MAX(id) FROM assignments WHERE stay_id = s.id)
            LEFT JOIN housekeepers h ON h.id = a.housekeeper_id
            WHERE r.property_id = ?""";

    private final JdbcClient jdbc;

    public RoomRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Every room of a property, by number. */
    public List<Row> all(long propertyId) {
        return jdbc.sql(SELECT + " ORDER BY r.no").param(propertyId).query(Row.class).list();
    }

    public Optional<Row> one(long propertyId, String no) {
        return jdbc.sql(SELECT + " AND r.no = ?").params(propertyId, no).query(Row.class).optional();
    }

    public void insert(long propertyId, String no, int floor, RoomType type) {
        jdbc.sql("INSERT INTO rooms (property_id, no, floor, type, out_of_order) VALUES (?, ?, ?, ?, FALSE)")
                .params(propertyId, no, floor, type.name()).update();
    }
}