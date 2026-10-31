create schema if not exists "dungeon-hub";
set schema "dungeon-hub";

create table carry_difficulty_bulk_price
(
    carry_difficulty BIGINT NOT NULL REFERENCES carry_difficulty (id) on delete cascade on update cascade,
    amount           INT    NOT NULL,
    price            INT    NOT NULL,
    PRIMARY KEY (carry_difficulty, amount)
);

insert into carry_difficulty_bulk_price (carry_difficulty, amount, price)
select id, bulk_amount, bulk_price
from carry_difficulty
where bulk_amount > 0
  and bulk_price > 0;

alter table carry_difficulty
    drop column bulk_price;

alter table carry_difficulty
    drop column bulk_amount;
