create schema if not exists `dungeon-hub`;
use `dungeon-hub`;

alter table ticket
    drop foreign key ticket_ibfk_1,
    drop foreign key ticket_ibfk_2,
    drop foreign key ticket_ibfk_3,
    drop foreign key ticket_ibfk_4;

alter table ticket
    add constraint fk_ticket_channel
        foreign key (channel) references discord_channel (id)
            on delete set null,
    add constraint fk_ticket_ticket_panel
        foreign key (ticket_panel) references ticket_panel (id)
            on delete cascade,
    add constraint fk_ticket_user
        foreign key (user_id) references discord_user (id)
            on delete cascade,
    add constraint fk_ticket_claimer
        foreign key (claimer) references discord_user (id)
            on delete set null;
