create schema if not exists "dungeon-hub";
set schema "dungeon-hub";

create table static_message
(
    id           BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    server       BIGINT                NOT NULL REFERENCES server (id),
    channel_id   BIGINT                NOT NULL,
    message_id   BIGINT,
    message_type SMALLINT              NOT NULL
);

create table static_message_object
(
    static_message BIGINT REFERENCES static_message (id),
    object_id      BIGINT NOT NULL
);

insert into static_message (server, channel_id, message_id, message_type)
    select distinct server, leaderboard_channel, null, 0 from carry_type where leaderboard_channel is not null;

insert into static_message_object (static_message, object_id)
    select static_message.id, carry_type.id
    from carry_type
    join static_message on static_message.server = carry_type.server and static_message.channel_id = carry_type.leaderboard_channel
    where leaderboard_channel is not null and message_type = 0;

alter table carry_type drop column if exists leaderboard_channel;
