create schema if not exists "dungeon-hub";
set search_path to "dungeon-hub";

alter table ticket
    drop constraint if exists ticket_channel_fkey,
    drop constraint if exists ticket_ticket_panel_fkey,
    drop constraint if exists ticket_user_id_fkey,
    drop constraint if exists ticket_claimer_fkey;

alter table ticket
    add constraint ticket_channel_fkey
        foreign key (channel) references discord_channel (id)
            on delete set null,
    add constraint ticket_ticket_panel_fkey
        foreign key (ticket_panel) references ticket_panel (id)
            on delete cascade,
    add constraint ticket_user_id_fkey
        foreign key (user_id) references discord_user (id)
            on delete cascade,
    add constraint ticket_claimer_fkey
        foreign key (claimer) references discord_user (id)
            on delete set null;
