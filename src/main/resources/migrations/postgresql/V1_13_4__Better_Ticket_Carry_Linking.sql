create schema if not exists "dungeon-hub";
set search_path to "dungeon-hub";

alter table ticket_panel
    add column related_carry_tier BIGINT REFERENCES carry_tier (id);

alter table ticket_panel
    add column related_carry_difficulty BIGINT REFERENCES carry_difficulty (id);

update ticket_panel
    set related_carry_tier = (
        select carry_tier.id from carry_tier where related_ticket_panel = ticket_panel.id
    );

alter table carry_tier
    drop column related_ticket_panel;