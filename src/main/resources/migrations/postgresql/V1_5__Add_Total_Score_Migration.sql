set search_path to "dungeon-hub";

create view score_sum AS
(
select *
from (SELECT score.id,
             score_type,
             (select server from carry_type where carry_type.id = score.carry_type) as server,
             sum(score)
             over (partition by score.id, server, score_type)                       as total_score
      from score
               join carry_type on score.carry_type = carry_type.id) as score_sum
group by id, score_type, server, total_score);
