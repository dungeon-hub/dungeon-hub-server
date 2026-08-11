create table carry_difficulty_history
(
    id               BIGSERIAL PRIMARY KEY,
    carry_difficulty BIGINT    NOT NULL REFERENCES carry_difficulty (id) on delete cascade on update cascade,
    price            BIGINT    NOT NULL,
    bulk_price       BIGINT,
    bulk_amount      BIGINT,
    score            BIGINT    NOT NULL,
    date_from        TIMESTAMP NOT NULL,
    date_to          TIMESTAMP NOT NULL
);

create index idx_carry_difficulty_history_period
    on carry_difficulty_history (carry_difficulty, date_from, date_to);
