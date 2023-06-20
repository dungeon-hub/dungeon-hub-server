create table carries
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    carrier          BIGINT REFERENCES carrier (id),
    player           BIGINT,
    approver         BIGINT,
    amount           mediumint,
    carry_difficulty BIGINT REFERENCES carry_difficulty (id) on delete cascade on update cascade,
    attachment_link  varchar(250),
    time             TIMESTAMP
);

create table log_queue
(
    id               BIGINT NOT NULL,
    carrier          BIGINT REFERENCES carrier (id),
    player           BIGINT,
    amount           mediumint,
    carry_difficulty BIGINT REFERENCES carry_difficulty (id) on delete cascade on update cascade,
    time             TIMESTAMP
);

create table log_approving_queue
(
    id               BIGINT NOT NULL,
    carrier          BIGINT REFERENCES carrier (id),
    player           BIGINT,
    amount           mediumint,
    carry_difficulty BIGINT REFERENCES carry_difficulty (id) on delete cascade on update cascade,
    attachment_link  varchar(250),
    time             TIMESTAMP
);

create table strikes
(
    id        BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    server_id BIGINT                NOT NULL,
    user      BIGINT                NOT NULL,
    striker   BIGINT,
    reason    varchar(250),
    time      TIMESTAMP             NOT NULL
);

create table score
(
    id         BIGINT NOT NULL REFERENCES carrier (id) on delete cascade on update cascade,
    carry_type BIGINT NOT NULL REFERENCES carry_type (id) on delete cascade on update cascade,
    score      BIGINT,
    primary key (id, carry_type)
);

create table alltime_score
(
    id         BIGINT NOT NULL REFERENCES carrier (id) on delete cascade on update cascade,
    carry_type BIGINT NOT NULL REFERENCES carry_type (id) on delete cascade on update cascade,
    score      BIGINT,
    primary key (id, carry_type)
);

create table event_score
(
    id         BIGINT NOT NULL REFERENCES carrier (id) on delete cascade on update cascade,
    carry_type BIGINT NOT NULL REFERENCES carry_type (id) on delete cascade on update cascade,
    score      BIGINT,
    primary key (id, carry_type)
);

insert into score(id, score, carry_type)
select old_score.id, score, ct.id
from slayer_score as old_score
         left join carry_type ct on ct.id = 5;

create table carry_type
(
    id                  BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    identifier          varchar(50)           NOT NULL,
    display_name        varchar(50)           NOT NULL,
    server              BIGINT                NOT NULL,
    log_channel         BIGINT,
    leaderboard_channel BIGINT,
    UNIQUE (identifier, server)
);

create table carry_tier
(
    id               BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    identifier       varchar(50)           NOT NULL,
    display_name     varchar(50)           NOT NULL,
    carry_type       BIGINT                NOT NULL REFERENCES carry_type (id) on delete cascade on update cascade,
    thumbnail_url    varchar(200),
    category         BIGINT,
    descriptive_name varchar(75),
    price_channel    BIGINT,
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
    UNIQUE (identifier, carry_tier)
);