create schema if not exists "dungeon-hub";
set search_path to "dungeon-hub";

alter table static_message add column primary_skyblock_profile UUID;
