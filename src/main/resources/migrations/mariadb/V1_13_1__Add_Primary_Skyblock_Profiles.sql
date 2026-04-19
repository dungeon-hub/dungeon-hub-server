create schema if not exists `dungeon-hub`;
use `dungeon-hub`;

alter table discord_user add column primary_skyblock_profile UUID;
