create table carries
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    serverId        BIGINT NOT NULL,
    carrier         BIGINT REFERENCES carrier (id),
    player          BIGINT,
    approver        BIGINT,
    amountOfCarries mediumint,
    carryDifficulty varchar(50),
    carryType       varchar(50),
    attachmentLink  varchar(250),
    time            TIMESTAMP
);

create table log_queue
(
    id              BIGINT NOT NULL,
    serverId        BIGINT NOT NULL,
    carrier         BIGINT REFERENCES carrier (id),
    player          BIGINT,
    amountOfCarries mediumint,
    carryDifficulty varchar(50),
    carryType       varchar(50),
    time            TIMESTAMP
);

create table log_approving_queue
(
    id              BIGINT NOT NULL,
    serverId        BIGINT NOT NULL,
    carrier         BIGINT REFERENCES carrier (id),
    player          BIGINT,
    amountOfCarries mediumint,
    carryDifficulty varchar(50),
    carryType       varchar(50),
    attachmentLink  varchar(250),
    time            TIMESTAMP
);

create table strikes
(
    id       BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    serverId BIGINT                NOT NULL,
    user     BIGINT                NOT NULL,
    striker  BIGINT,
    reason   varchar(250),
    time     TIMESTAMP             NOT NULL
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
    id           BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    identifier   varchar(50)           NOT NULL,
    displayName  varchar(50),
    server       BIGINT                NOT NULL,
    logChannel   BIGINT,
    thumbnailUrl varchar(200),
    UNIQUE (identifier, server)
);

create table carry_tier
(
    id           BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    identifier   varchar(50),
    displayName  varchar(50),
    carry_type   BIGINT                NOT NULL REFERENCES carry_type (id) on delete cascade on update cascade,
    thumbnailUrl varchar(200),
    UNIQUE (identifier, carry_type)
);

create table carry_difficulty
(
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    identifier varchar(50),
    displayName varchar(50),
    carry_tier BIGINT NOT NULL REFERENCES carry_tier(id) on delete cascade on update cascade
);