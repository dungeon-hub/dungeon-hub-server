set search_path to "dungeon-hub";

create table warns
(
    id           SERIAL    NOT NULL PRIMARY KEY,
    server_id    BIGINT    NOT NULL REFERENCES server (id),
    warned_user  BIGINT    NOT NULL REFERENCES discord_user (id),
    striker      BIGINT    NOT NULL REFERENCES discord_user (id),
    warning_type BIGINT    NOT NULL,
    reason       varchar(250),
    active       BOOLEAN   NOT NULL,
    time         TIMESTAMP NOT NULL
);

create table warn_proofs
(
    id        SERIAL       NOT NULL PRIMARY KEY,
    warn_id   BIGINT       NOT NULL REFERENCES warns (id),
    proof     varchar(200) NOT NULL,
    submitter BIGINT       NOT NULL REFERENCES discord_user (id)
);

insert into warns(id, server_id, warned_user, striker, warning_type, reason, active, time)
select strikes.id,
       strikes.server_id,
       strikes."user",
       strikes.striker,
       0,
       strikes.reason,
       true,
       strikes.time
from strikes;

drop table if exists strikes;