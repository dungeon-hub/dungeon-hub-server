create schema if not exists "dungeon-hub";
set schema "dungeon-hub";

create table server
(
    id BIGINT PRIMARY KEY
);

create table discord_user
(
    id           BIGINT PRIMARY KEY,
    minecraft_id UUID
);

create table carrier
(
    id          BIGINT PRIMARY KEY REFERENCES discord_user (id),
    f4          boolean,
    f5          boolean,
    f6          boolean,
    f7          boolean,
    master_mode boolean,
    eman_t3     boolean,
    eman_t4     boolean,
    blaze_t2    boolean,
    blaze_t3    boolean,
    blaze_t4    boolean,
    basic       boolean,
    hot         boolean,
    burning     boolean,
    fiery       boolean,
    infernal    boolean
);

create table discord_role
(
    id            BIGINT PRIMARY KEY,
    name_schema   varchar(100),
    role_group    BIGINT  REFERENCES discord_role (id) ON DELETE SET NULL,
    server        BIGINT  NOT NULL REFERENCES server (id),
    verified_role BOOLEAN NOT NULL DEFAULT 0,
    UNIQUE (server, id)
);

create table carry_type
(
    id                  BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    identifier          varchar(50)           NOT NULL,
    display_name        varchar(50)           NOT NULL,
    server              BIGINT                NOT NULL REFERENCES server (id),
    log_channel         BIGINT,
    leaderboard_channel BIGINT,
    event_active        BOOLEAN,
    UNIQUE (identifier, server)
);

create table carry_tier
(
    id                BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    identifier        varchar(50)           NOT NULL,
    display_name      varchar(50)           NOT NULL,
    carry_type        BIGINT                NOT NULL REFERENCES carry_type (id) on delete cascade on update cascade,
    thumbnail_url     varchar(200),
    category          BIGINT,
    descriptive_name  varchar(75),
    price_channel     BIGINT,
    price_title       varchar(75),
    price_description varchar(200),
    UNIQUE (identifier, carry_type),
    UNIQUE (category)
);

create table carry_difficulty
(
    id            BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    identifier    varchar(50)           NOT NULL,
    display_name  varchar(50)           NOT NULL,
    carry_tier    BIGINT                NOT NULL REFERENCES carry_tier (id) on delete cascade on update cascade,
    thumbnail_url varchar(200),
    score         BIGINT                NOT NULL,
    price         BIGINT                NOT NULL,
    bulk_price    BIGINT,
    bulk_amount   BIGINT,
    price_name    varchar(75),
    UNIQUE (identifier, carry_tier)
);

create table carry_queue
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    queue_step       BIGINT NOT NULL,
    relation_id      BIGINT,
    carrier          BIGINT REFERENCES discord_user (id),
    player           BIGINT REFERENCES discord_user (id),
    amount           mediumint,
    carry_difficulty BIGINT REFERENCES carry_difficulty (id) on delete cascade on update cascade,
    attachment_link  varchar(250),
    time             TIMESTAMP
);

create table carry
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    carrier          BIGINT REFERENCES discord_user (id),
    player           BIGINT REFERENCES discord_user (id),
    approver         BIGINT,
    amount           mediumint,
    carry_difficulty BIGINT REFERENCES carry_difficulty (id) on delete cascade on update cascade,
    attachment_link  varchar(250),
    time             TIMESTAMP
);

create table strikes
(
    id        BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    server_id BIGINT                NOT NULL,
    `user`    BIGINT                NOT NULL,
    striker   BIGINT,
    reason    varchar(250),
    time      TIMESTAMP             NOT NULL
);

create table score
(
    id         BIGINT   NOT NULL REFERENCES discord_user (id) on delete cascade on update cascade,
    carry_type BIGINT   NOT NULL REFERENCES carry_type (id) on delete cascade on update cascade,
    score_type SMALLINT NOT NULL,
    score      BIGINT,
    primary key (id, carry_type, score_type)
);

commit;