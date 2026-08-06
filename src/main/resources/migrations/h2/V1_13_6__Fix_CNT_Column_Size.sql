create schema if not exists "dungeon-hub";
set schema "dungeon-hub";

alter table cnt_request
    alter column coin_value text NOT NULL;

alter table cnt_request
    alter column description text NOT NULL;

alter table cnt_request
    alter column requirement text NOT NULL;
