set schema "dungeon-hub";

alter table cnt_request
    add column completed BOOLEAN NOT NULL default false;