create schema if not exists "dungeon-hub";
set search_path to "dungeon-hub";

-- the permissions were stored as Kord bitsets, which only ever held the defaults
ALTER TABLE ticket_panel
    DROP COLUMN support_team_allowed_permissions,
    DROP COLUMN support_team_denied_permissions,
    DROP COLUMN additional_roles_allowed_permissions,
    DROP COLUMN additional_roles_denied_permissions,
    DROP COLUMN creator_allowed_permissions,
    DROP COLUMN creator_denied_permissions,
    DROP COLUMN claimer_allowed_permissions,
    DROP COLUMN claimer_denied_permissions,
    DROP COLUMN everyone_allowed_permissions,
    DROP COLUMN everyone_denied_permissions,
    ADD COLUMN permissions TEXT;
