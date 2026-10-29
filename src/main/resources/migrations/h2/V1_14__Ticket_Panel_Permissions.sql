create schema if not exists "dungeon-hub";
set schema "dungeon-hub";

-- the old Kord bitsets only ever held the defaults, so they are not migrated
ALTER TABLE ticket_panel DROP COLUMN support_team_allowed_permissions;
ALTER TABLE ticket_panel DROP COLUMN support_team_denied_permissions;
ALTER TABLE ticket_panel DROP COLUMN additional_roles_allowed_permissions;
ALTER TABLE ticket_panel DROP COLUMN additional_roles_denied_permissions;
ALTER TABLE ticket_panel DROP COLUMN creator_allowed_permissions;
ALTER TABLE ticket_panel DROP COLUMN creator_denied_permissions;
ALTER TABLE ticket_panel DROP COLUMN claimer_allowed_permissions;
ALTER TABLE ticket_panel DROP COLUMN claimer_denied_permissions;
ALTER TABLE ticket_panel DROP COLUMN everyone_allowed_permissions;
ALTER TABLE ticket_panel DROP COLUMN everyone_denied_permissions;

-- the enums are stored by their ordinal, like the other enums in this schema
create table ticket_panel_action_permission
(
    ticket_panel  BIGINT  NOT NULL REFERENCES ticket_panel (id) ON DELETE CASCADE,
    phase         INT     NOT NULL,
    candidate     INT     NOT NULL,
    ticket_action INT     NOT NULL,
    allowed       BOOLEAN NOT NULL,
    PRIMARY KEY (ticket_panel, phase, candidate, ticket_action)
);

create table ticket_panel_channel_permission
(
    ticket_panel       BIGINT  NOT NULL REFERENCES ticket_panel (id) ON DELETE CASCADE,
    phase              INT     NOT NULL,
    candidate          INT     NOT NULL,
    channel_permission INT     NOT NULL,
    allowed            BOOLEAN NOT NULL,
    PRIMARY KEY (ticket_panel, phase, candidate, channel_permission)
);
