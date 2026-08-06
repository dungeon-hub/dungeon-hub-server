set search_path to "dungeon-hub";

create table discord_role_group
(
    id           SERIAL PRIMARY KEY,
    discord_role BIGINT REFERENCES discord_role (id) ON DELETE CASCADE ON UPDATE CASCADE,
    role_group   BIGINT REFERENCES discord_role (id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE (discord_role, role_group)
);

insert into discord_role_group(discord_role, role_group) (select id, role_group from discord_role where role_group is not null);

alter table discord_role
    drop constraint discord_role_role_group_fkey;
alter table discord_role
    drop column role_group;