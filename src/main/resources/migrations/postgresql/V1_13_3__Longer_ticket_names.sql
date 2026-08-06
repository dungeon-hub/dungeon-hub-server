create schema if not exists "dungeon-hub";
set search_path to "dungeon-hub";

ALTER TABLE ticket_panel
    ALTER COLUMN open_channel_name type varchar(100);

ALTER TABLE ticket_panel
    ALTER COLUMN claimed_channel_name type VARCHAR(100);

ALTER TABLE ticket_panel
    ALTER COLUMN closed_channel_name type VARCHAR(100);