create schema if not exists `dungeon-hub`;
use `dungeon-hub`;

ALTER TABLE ticket_panel
    MODIFY open_channel_name VARCHAR(100) NULL;

ALTER TABLE ticket_panel
    MODIFY claimed_channel_name VARCHAR(100) NULL;

ALTER TABLE ticket_panel
    MODIFY closed_channel_name VARCHAR(100) NULL;