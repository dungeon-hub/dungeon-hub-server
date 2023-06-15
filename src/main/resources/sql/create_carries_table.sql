create table carries
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    serverId        BIGINT NOT NULL,
    carrier         BIGINT REFERENCES carrier(id),
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
    carrier         BIGINT REFERENCES carrier(id),
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
    carrier         BIGINT REFERENCES carrier(id),
    player          BIGINT,
    amountOfCarries mediumint,
    carryDifficulty varchar(50),
    carryType       varchar(50),
    attachmentLink  varchar(250),
    time            TIMESTAMP
);

create table dungeon_score
(
    id       BIGINT NOT NULL REFERENCES carrier(id),
    serverId BIGINT NOT NULL,
    score    BIGINT,
    primary key(id,serverId)
);

create table slayer_score
(
    id    BIGINT NOT NULL REFERENCES carrier(id),
    serverId BIGINT NOT NULL,
    score BIGINT,
    primary key(id,serverId)
);

create table kuudra_score
(
    id    BIGINT NOT NULL REFERENCES carrier(id),
    serverId BIGINT NOT NULL,
    score BIGINT,
    primary key(id,serverId)
);

create table alltime_dungeon_score
(
    id    BIGINT NOT NULL REFERENCES carrier(id),
    serverId BIGINT NOT NULL,
    score BIGINT,
    primary key(id,serverId)
);

create table alltime_slayer_score
(
    id    BIGINT NOT NULL REFERENCES carrier(id),
    serverId BIGINT NOT NULL,
    score BIGINT,
    primary key(id,serverId)
);

create table alltime_kuudra_score
(
    id    BIGINT NOT NULL REFERENCES carrier(id),
    serverId BIGINT NOT NULL,
    score BIGINT,
    primary key(id,serverId)
);

create table event_dungeon_score
(
    id    BIGINT NOT NULL REFERENCES carrier(id),
    serverId BIGINT NOT NULL,
    score BIGINT,
    primary key(id,serverId)
);

create table event_slayer_score
(
    id    BIGINT NOT NULL REFERENCES carrier(id),
    serverId BIGINT NOT NULL,
    score BIGINT,
    primary key(id,serverId)
);

create table strikes
(
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    serverId BIGINT NOT NULL,
    user BIGINT NOT NULL,
    striker BIGINT,
    reason varchar(250),
    time TIMESTAMP NOT NULL
);

create table carry_type
(
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    identifier varchar(250),
    server BIGINT NOT NULL,
    logChannel BIGINT
    UNIQUE (identifier, server)
);