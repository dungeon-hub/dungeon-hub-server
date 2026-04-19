create schema if not exists api;
set search_path to api;

drop table if exists refresh_token;

drop table if exists user_groups;

drop table if exists group_privileges;

drop table if exists "user";

drop table if exists "group";

drop table if exists privilege;

DROP EXTENSION IF EXISTS "uuid-ossp";

drop schema if exists api;

commit;