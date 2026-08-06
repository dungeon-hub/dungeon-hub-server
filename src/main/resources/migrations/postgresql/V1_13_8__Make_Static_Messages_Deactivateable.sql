create schema if not exists "dungeon-hub";
set search_path to "dungeon-hub";

ALTER TABLE static_message
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
