create schema if not exists api;
use api;

drop table if exists refresh_token;

drop table if exists user_groups;

drop table if exists group_privileges;

drop table if exists user;

drop table if exists `group`;

drop table if exists privilege;

drop schema if exists api;

commit;