create table carries
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
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
    id    BIGINT NOT NULL PRIMARY KEY,
    score BIGINT
);

create table slayer_score
(
    id    BIGINT NOT NULL PRIMARY KEY REFERENCES carrier(id),
    score BIGINT
);

create table kuudra_score
(
    id    BIGINT NOT NULL PRIMARY KEY REFERENCES carrier(id),
    score BIGINT
);