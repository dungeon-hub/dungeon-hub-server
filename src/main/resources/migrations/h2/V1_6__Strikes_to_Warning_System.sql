set schema "dungeon-hub";

create table warns
(
    id           BIGINT AUTO_INCREMENT               NOT NULL PRIMARY KEY,
    server_id    BIGINT REFERENCES server (id)       NOT NULL,
    warned_user       BIGINT REFERENCES discord_user (id) NOT NULL,
    striker      BIGINT REFERENCES discord_user (id) NOT NULL,
    warning_type BIGINT                              NOT NULL,
    reason       varchar(250),
    active       BOOLEAN                             NOT NULL,
    time         TIMESTAMP                           NOT NULL
);

create table warn_proofs
(
    id        BIGINT                              NOT NULL AUTO_INCREMENT PRIMARY KEY,
    warn_id   BIGINT                              NOT NULL REFERENCES warns (id),
    proof     varchar(200)                        NOT NULL,
    submitter BIGINT REFERENCES discord_user (id) NOT NULL
);

insert into warns(id, server_id, warned_user, striker, warning_type, reason, active, time)
select strikes.id, strikes.server_id, strikes.`user`, strikes.striker, 0, strikes.reason, true, strikes.time
from strikes;

drop table if exists strikes;