set search_path to "dungeon-hub";

create table role_requirement
(
    id               SERIAL NOT NULL PRIMARY KEY,
    discord_role     BIGINT NOT NULL REFERENCES discord_role (id),
    requirement_type INT    NOT NULL,
    comparison       INT    NOT NULL,
    count            INT    NOT NULL,
    extra_data       VARCHAR(255)
);