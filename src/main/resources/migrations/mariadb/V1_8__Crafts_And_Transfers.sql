use `dungeon-hub`;

create table cnt_request
(
    id           BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    server_id    BIGINT                NOT NULL REFERENCES server (id),
    message_id   BIGINT                NOT NULL,
    user_id      BIGINT                NOT NULL REFERENCES discord_user (id),
    claimer_id   BIGINT REFERENCES discord_user (id),
    request_time TIMESTAMP             NOT NULL,
    coin_value   VARCHAR(50)           NOT NULL,
    description  VARCHAR(255)          NOT NULL,
    requirement  VARCHAR(100)          NOT NULL
);