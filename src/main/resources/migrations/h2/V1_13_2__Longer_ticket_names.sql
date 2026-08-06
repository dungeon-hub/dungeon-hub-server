create schema if not exists "dungeon-hub";
set schema "dungeon-hub";

ALTER TABLE ticket_panel
    alter column open_channel_name VARCHAR(100) NULL;

ALTER TABLE ticket_panel
    alter column claimed_channel_name VARCHAR(100) NULL;

ALTER TABLE ticket_panel
    alter column closed_channel_name VARCHAR(100) NULL;