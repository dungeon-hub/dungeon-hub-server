create schema if not exists "dungeon-hub";
set search_path to "dungeon-hub";

alter table cnt_request
    alter column coin_value type text;

alter table cnt_request
    alter column description type text;

alter table cnt_request
    alter column requirement type text;
