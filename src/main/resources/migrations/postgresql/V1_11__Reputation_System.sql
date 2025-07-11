set search_path to "dungeon-hub";

create table reputation
(
    id         SERIAL NOT NULL PRIMARY KEY,
    server_id  BIGINT                NOT NULL REFERENCES server (id),
    user_id    BIGINT                NOT NULL REFERENCES discord_user (id),
    reputor_id BIGINT REFERENCES discord_user (id),
    rep_amount INT                   NOT NULL,
    rep_reason VARCHAR(512),
    time       TIMESTAMP             NOT NULL
);