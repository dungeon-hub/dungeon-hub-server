create schema if not exists `dungeon-hub`;
use `dungeon-hub`;

ALTER TABLE static_message
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
