use `dungeon-hub`;

create table purge_type
(
    id           BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    identifier   varchar(50)           NOT NULL,
    display_name varchar(50)           NOT NULL,
    carry_type   BIGINT                NOT NULL REFERENCES carry_type (id),
    UNIQUE (identifier, carry_type)
);

create table purge_type_role
(
    discord_role BIGINT REFERENCES discord_role (id) ON DELETE CASCADE ON UPDATE CASCADE,
    purge_type   BIGINT REFERENCES purge_type (id) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY (discord_role, purge_type)
);

drop table carrier;