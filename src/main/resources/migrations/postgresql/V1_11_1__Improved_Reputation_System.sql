set search_path to "dungeon-hub";

alter table reputation
    add column active BOOLEAN NOT NULL DEFAULT TRUE;

alter table reputation
    add column cnt_request BIGINT references cnt_request(id);