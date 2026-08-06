set schema "dungeon-hub";

alter table DISCORD_ROLE ADD COLUMN role_action BIGINT NOT NULL DEFAULT 0;

update DISCORD_ROLE set DISCORD_ROLE.role_action = 1 where VERIFIED_ROLE = true;

alter table DISCORD_ROLE drop column VERIFIED_ROLE;