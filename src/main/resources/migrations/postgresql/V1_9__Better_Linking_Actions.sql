set search_path to "dungeon-hub";

alter table discord_role ADD COLUMN role_action BIGINT NOT NULL DEFAULT 0;

update discord_role set role_action = 1 where verified_role = true;

alter table discord_role drop column verified_role;