create schema if not exists api;
use api;

create table user
(
    id         BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    login_name varchar(100)          not null,
    password   varchar(500)          not null,
    enabled    boolean
);

create table `group`
(
    id   BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    name varchar(50)
);

create table privilege
(
    id   BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    name varchar(150)
);

create table user_groups
(
    user_id  BIGINT REFERENCES user (id),
    group_id BIGINT REFERENCES `group` (id),
    UNIQUE (user_id, group_id)
);

create table group_privileges
(
    group_id     BIGINT REFERENCES `group` (id),
    privilege_id BIGINT REFERENCES privilege (id),
    UNIQUE (group_id, privilege_id)
);

create table refresh_token
(
    user        BIGINT    NOT NULL REFERENCES user (id),
    token       UUID      NOT NULL default UUID(),
    valid_until TIMESTAMP NOT NULL
);

commit;