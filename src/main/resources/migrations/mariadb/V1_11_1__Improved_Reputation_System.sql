use `dungeon-hub`;

alter table reputation
    add column active BOOLEAN NOT NULL DEFAULT TRUE;

alter table reputation
    add column cnt_request BIGINT REFERENCES cnt_request (id);
