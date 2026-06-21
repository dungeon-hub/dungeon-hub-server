create schema if not exists "dungeon-hub";
set search_path to "dungeon-hub";

alter table discord_channel
    alter column name type varchar(150);

alter table ticket_form_response
    alter column response_value type text;