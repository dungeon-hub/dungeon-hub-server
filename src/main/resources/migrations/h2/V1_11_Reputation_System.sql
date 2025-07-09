set schema "dungeon-hub";

create table reputation
(
    id         BIGINT AUTO_INCREMENT               NOT NULL PRIMARY KEY,
    server_id  BIGINT REFERENCES server (id)       NOT NULL,
    user_id    BIGINT REFERENCES discord_user (id) NOT NULL,
    reputor_id BIGINT REFERENCES discord_user (id),
    rep_amount INT                                 NOT NULL,
    rep_reason VARCHAR(512)
);
