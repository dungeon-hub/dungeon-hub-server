set schema "dungeon-hub";

alter table cnt_request
    add column request_type INT NOT NULL default 0;

alter table cnt_request
    alter column request_type DROP DEFAULT;