create schema if not exists "dungeon-hub";
set search_path to "dungeon-hub";

ALTER TABLE ticket_panel
    ADD COLUMN open_ticket_limit INT NULL;

ALTER TABLE ticket_panel
    ADD COLUMN closed_ticket_limit INT NULL;
