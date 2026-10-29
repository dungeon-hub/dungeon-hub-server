create schema if not exists "dungeon-hub";
set schema "dungeon-hub";

-- the permissions were stored as Kord bitsets, which only ever held the defaults
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

ALTER TABLE ticket_panel ADD COLUMN permissions CLOB;
