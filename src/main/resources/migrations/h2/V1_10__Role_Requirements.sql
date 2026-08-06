set schema "dungeon-hub";

create table role_requirement
(
    id               BIGINT AUTO_INCREMENT               NOT NULL PRIMARY KEY,
    discord_role     BIGINT REFERENCES discord_role (id) NOT NULL,
    requirement_type INT                                 NOT NULL,
    comparison       INT                                 NOT NULL,
    count            INT                                 NOT NULL,
    extra_data       VARCHAR(255)
);