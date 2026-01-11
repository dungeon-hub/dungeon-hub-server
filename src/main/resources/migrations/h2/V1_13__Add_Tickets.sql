create schema if not exists "dungeon-hub";
set schema "dungeon-hub";

alter table static_message add column embed_override CLOB;

create table discord_channel
(
    id      BIGINT PRIMARY KEY,
    name    varchar(50),
    server  BIGINT  NOT NULL REFERENCES server (id),
    deleted boolean NOT NULL default false
);

create table ticket_panel
(
    id                                   BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    name                                 varchar(50)           NOT NULL,
    display_name                         varchar(50),
    emoji                                varchar(50),
    server                               BIGINT                NOT NULL REFERENCES server (id),
    closeable                            boolean               NOT NULL,
    close_confirmation                   boolean               NOT NULL,
    claimable                            boolean               NOT NULL,
    open_channel_name                    varchar(50),
    claimed_channel_name                 varchar(50),
    closed_channel_name                  varchar(50),
    transcript_channel                   BIGINT REFERENCES discord_channel (id),
    ticket_message                       CLOB,
    requires_linking                     boolean               NOT NULL,
    close_transcript_target              INT                   NOT NULL DEFAULT 0,
    delete_transcript_target             INT                   NOT NULL DEFAULT 0,
    user_transcript_dm                   CLOB,

    support_team_allowed_permissions     VARBINARY,
    support_team_denied_permissions      VARBINARY,
    additional_roles_allowed_permissions VARBINARY,
    additional_roles_denied_permissions  VARBINARY,
    creator_allowed_permissions          VARBINARY,
    creator_denied_permissions           VARBINARY,
    claimer_allowed_permissions          VARBINARY,
    claimer_denied_permissions           VARBINARY,
    everyone_allowed_permissions         VARBINARY,
    everyone_denied_permissions          VARBINARY
);

create table ticket_panel_support_role
(
    ticket_panel BIGINT REFERENCES ticket_panel (id),
    support_role BIGINT NOT NULL REFERENCES discord_role (id)
);

create table ticket_panel_additional_role
(
    ticket_panel    BIGINT REFERENCES ticket_panel (id),
    additional_role BIGINT NOT NULL REFERENCES discord_role (id)
);

create table ticket_panel_open_category
(
    ticket_panel  BIGINT REFERENCES ticket_panel (id),
    open_category BIGINT NOT NULL
);

create table ticket_panel_closed_category
(
    ticket_panel    BIGINT REFERENCES ticket_panel (id),
    closed_category BIGINT NOT NULL
);

create table ticket
(
    id           BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    state        SMALLINT              NOT NULL,
    channel      BIGINT REFERENCES discord_channel (id),
    ticket_panel BIGINT                NOT NULL REFERENCES ticket_panel (id),
    user_id      BIGINT                NOT NULL REFERENCES discord_user (id),
    claimer      BIGINT REFERENCES discord_user (id),
    created      TIMESTAMP             NOT NULL
);

alter table carry_tier
    add column related_ticket_panel BIGINT REFERENCES ticket_panel (id);
