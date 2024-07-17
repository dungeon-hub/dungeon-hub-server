set schema "dungeon-hub";

create table warning_punishment
(
    id           BIGINT AUTO_INCREMENT         NOT NULL PRIMARY KEY,
    server_id    BIGINT REFERENCES server (id) NOT NULL,
    warning_type BIGINT                        NOT NULL,
    comparison   BIGINT                        NOT NULL,
    count        INT                           NOT NULL,
    action       BIGINT                        NOT NULL,
    extra_data   VARCHAR(75)
);