set schema "dungeon-hub";

create view score_sum AS
(
SELECT *
from (SELECT id,
             score_type,
             (select SERVER from carry_type where carry_type.id = score.carry_type) as server,
             sum(score)
                 over (partition by id, (select SERVER from carry_type where carry_type.id = score.carry_type), score_type)                         as total_score
      from score) as score_sum
group by id, score_type);