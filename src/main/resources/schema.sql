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
