set search_path to "dungeon-hub";

alter table cnt_request
    add column completed boolean not null default false;