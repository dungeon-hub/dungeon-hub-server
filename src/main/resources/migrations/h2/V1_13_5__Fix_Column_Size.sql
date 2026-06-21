create schema if not exists "dungeon-hub";
set schema "dungeon-hub";

alter table discord_channel
    alter column name varchar(150);

alter table ticket_form_response
    alter column response_value text NOT NULL;