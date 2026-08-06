create schema if not exists "dungeon-hub";
set search_path to "dungeon-hub";

insert into static_message (server, channel_id, message_id, message_type)
select distinct server, price_channel, cast(null as bigint), 4
from carry_tier
         join carry_type ct on ct.id = carry_tier.carry_type
where price_channel is not null;

insert into static_message_object (static_message, object_id)
select static_message.id, carry_tier.id
from carry_tier
         join carry_type on carry_type.id = carry_tier.carry_type
         join static_message on static_message.server = carry_type.server and static_message.channel_id = carry_tier.price_channel
where price_channel is not null and message_type = 4;

alter table carry_tier drop column if exists price_channel;
