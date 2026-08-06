create schema if not exists api;
set search_path to api;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

create table "user"
(
    id         SERIAL       NOT NULL PRIMARY KEY,
    login_name varchar(100) not null,
    password   varchar(500) not null,
    enabled    boolean
);

create table "group"
(
    id   SERIAL NOT NULL PRIMARY KEY,
    name varchar(50)
);

create table privilege
(
    id   SERIAL NOT NULL PRIMARY KEY,
    name varchar(150)
);

create table user_groups
(
    user_id  BIGINT REFERENCES "user" (id),
    group_id BIGINT REFERENCES "group" (id),
    UNIQUE (user_id, group_id)
);

create table group_privileges
(
    group_id     BIGINT REFERENCES "group" (id),
    privilege_id BIGINT REFERENCES privilege (id),
    UNIQUE (group_id, privilege_id)
);

create table refresh_token
(
    "user"      BIGINT    NOT NULL REFERENCES "user" (id),
    token       UUID      NOT NULL default uuid_generate_v1(),
    valid_until TIMESTAMP NOT NULL
);

commit;