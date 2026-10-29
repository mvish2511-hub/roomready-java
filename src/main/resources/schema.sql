-- The property, its rooms and its housekeepers. Run at every start-up, so every table is created only if
-- missing. A room's state is not stored: it is derived from its rows (stays, assignments, ...).

CREATE TABLE IF NOT EXISTS properties (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    slug VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL
);

CREATE TABLE IF NOT EXISTS rooms (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_id  BIGINT      NOT NULL REFERENCES properties (id),
    no           VARCHAR(8)  NOT NULL,
    floor        INT         NOT NULL,
    type         VARCHAR(10) NOT NULL,
    out_of_order BOOLEAN     NOT NULL,
    UNIQUE (property_id, no)
);

CREATE TABLE IF NOT EXISTS housekeepers (
    id          VARCHAR(8)  PRIMARY KEY,
    property_id BIGINT      NOT NULL REFERENCES properties (id),
    name        VARCHAR(60) NOT NULL
);

-- A guest's stay. The current stay of a room is its latest; it is open while checked_out_at is null. The next
-- guest's arrival, when the desk knows it, is kept for the readiness margin.
CREATE TABLE IF NOT EXISTS stays (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_id    BIGINT                   NOT NULL REFERENCES properties (id),
    room_id        BIGINT                   NOT NULL REFERENCES rooms (id),
    guest_ref      VARCHAR(20)              NOT NULL,
    checked_in_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    checked_out_at TIMESTAMP WITH TIME ZONE,
    arrival_at     TIMESTAMP WITH TIME ZONE
);

-- Who cleans a room after a stay, and the clean's stamps. clean_minutes is stamped once, at finish.
CREATE TABLE IF NOT EXISTS assignments (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_id    BIGINT                   NOT NULL REFERENCES properties (id),
    room_id        BIGINT                   NOT NULL REFERENCES rooms (id),
    stay_id        BIGINT                   NOT NULL REFERENCES stays (id),
    housekeeper_id VARCHAR(8)               NOT NULL REFERENCES housekeepers (id),
    assigned_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    started_at     TIMESTAMP WITH TIME ZONE,
    finished_at    TIMESTAMP WITH TIME ZONE,
    clean_minutes  INT
);

-- An inspection of a clean. A failed one closes the assignment: the room is dirty again.
CREATE TABLE IF NOT EXISTS inspections (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_id   BIGINT                   NOT NULL REFERENCES properties (id),
    room_id       BIGINT                   NOT NULL REFERENCES rooms (id),
    assignment_id BIGINT                   NOT NULL REFERENCES assignments (id),
    inspector_id  VARCHAR(8)               NOT NULL REFERENCES housekeepers (id),
    result        VARCHAR(4)               NOT NULL,
    inspected_at  TIMESTAMP WITH TIME ZONE NOT NULL
);
