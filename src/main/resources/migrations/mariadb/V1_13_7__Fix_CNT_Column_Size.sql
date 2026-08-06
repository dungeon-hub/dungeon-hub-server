create schema if not exists `dungeon-hub`;
use `dungeon-hub`;

alter table cnt_request
    modify coin_value text NOT NULL;

alter table cnt_request
    modify description text NOT NULL;

alter table cnt_request
    modify requirement text NOT NULL;
