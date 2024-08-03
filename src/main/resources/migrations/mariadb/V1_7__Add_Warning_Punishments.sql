use `dungeon-hub`;

create table warn_punishment
(
    id           BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    server_id    BIGINT                NOT NULL REFERENCES server (id),
    warning_type BIGINT                NOT NULL,
    comparison   BIGINT                NOT NULL,
    count        INT                   NOT NULL,
    action       BIGINT                NOT NULL,
    extra_data   VARCHAR(75)
);