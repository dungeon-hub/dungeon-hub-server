create schema if not exists `dungeon-hub`;
use `dungeon-hub`;

alter table discord_channel
    modify name varchar(150);

alter table ticket_form_response
    modify response_value text NOT NULL;