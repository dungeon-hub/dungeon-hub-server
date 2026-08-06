create schema if not exists "dungeon-hub";
set schema "dungeon-hub";

alter table discord_user add column primary_skyblock_profile UUID;
