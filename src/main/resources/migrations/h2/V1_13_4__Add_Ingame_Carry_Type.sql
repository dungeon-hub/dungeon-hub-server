create schema if not exists "dungeon-hub";
set schema "dungeon-hub";

alter table carry_difficulty
    add column ingame_carry_type varchar(50);

alter table carry_queue
    add column notified boolean not null default true
