create schema if not exists `dungeon-hub`;
use `dungeon-hub`;

alter table static_message add column embed_override JSON;

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
    ticket_message                       JSON,
    requires_linking                     boolean               NOT NULL,
    close_transcript_target              INT                   NOT NULL DEFAULT 0,
    delete_transcript_target             INT                   NOT NULL DEFAULT 0,
    user_transcript_dm                   JSON,

    support_team_allowed_permissions     VARBINARY(32),
    support_team_denied_permissions      VARBINARY(32),
    additional_roles_allowed_permissions VARBINARY(32),
    additional_roles_denied_permissions  VARBINARY(32),
    creator_allowed_permissions          VARBINARY(32),
    creator_denied_permissions           VARBINARY(32),
    claimer_allowed_permissions          VARBINARY(32),
    claimer_denied_permissions           VARBINARY(32),
    everyone_allowed_permissions         VARBINARY(32),
    everyone_denied_permissions          VARBINARY(32)
);

create table ticket_panel_form
(
    ticket_panel BIGINT NOT NULL,
    form_type    INT,
    data         JSON,
    ordinal      INT NOT NULL,
    PRIMARY KEY (ticket_panel, ordinal),
    FOREIGN KEY (ticket_panel) REFERENCES ticket_panel (id)
);

create table ticket_panel_support_role
(
    ticket_panel BIGINT NOT NULL,
    support_role BIGINT NOT NULL,
    PRIMARY KEY (ticket_panel, support_role),
    FOREIGN KEY (ticket_panel) REFERENCES ticket_panel (id),
    FOREIGN KEY (support_role) REFERENCES discord_role (id)
);

create table ticket_panel_additional_role
(
    ticket_panel    BIGINT NOT NULL,
    additional_role BIGINT NOT NULL,
    PRIMARY KEY (ticket_panel, additional_role),
    FOREIGN KEY (ticket_panel) REFERENCES ticket_panel (id),
    FOREIGN KEY (additional_role) REFERENCES discord_role (id)
);

create table ticket_panel_open_category
(
    ticket_panel  BIGINT NOT NULL,
    open_category BIGINT NOT NULL,
    PRIMARY KEY (ticket_panel, open_category),
    FOREIGN KEY (ticket_panel) REFERENCES ticket_panel (id),
    FOREIGN KEY (open_category) REFERENCES discord_channel (id)
);

create table ticket_panel_closed_category
(
    ticket_panel    BIGINT NOT NULL,
    closed_category BIGINT NOT NULL,
    PRIMARY KEY (ticket_panel, closed_category),
    FOREIGN KEY (ticket_panel) REFERENCES ticket_panel (id),
    FOREIGN KEY (closed_category) REFERENCES discord_channel (id)
);

create table ticket
(
    id           BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    state        SMALLINT              NOT NULL,
    channel      BIGINT,
    ticket_panel BIGINT                NOT NULL,
    user_id      BIGINT                NOT NULL,
    claimer      BIGINT,
    created      TIMESTAMP             NOT NULL,
    FOREIGN KEY (channel) REFERENCES discord_channel (id),
    FOREIGN KEY (ticket_panel) REFERENCES ticket_panel (id),
    FOREIGN KEY (user_id) REFERENCES discord_user (id),
    FOREIGN KEY (claimer) REFERENCES discord_user (id)
);

create table ticket_form_response
(
    ticket         BIGINT       NOT NULL,
    ordinal        INT          NOT NULL,
    custom_id      varchar(255) NOT NULL,
    response_value varchar(255) NOT NULL,
    PRIMARY KEY (ticket, ordinal),
    FOREIGN KEY (ticket) REFERENCES ticket (id)
);

alter table carry_tier
    add column related_ticket_panel BIGINT,
    add FOREIGN KEY (related_ticket_panel) REFERENCES ticket_panel (id);
