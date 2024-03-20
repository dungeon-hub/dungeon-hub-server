use `dungeon-hub`;

# Create table for roles
CREATE TABLE carry_role
(
    id              BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    discord_role_id BIGINT                NOT NULL,
    display_name    VARCHAR(100)          NOT NULL,
    manual          BIT                   NOT NULL,
    enabled         BIT                   NOT NULL,
    FOREIGN KEY (discord_role_id) REFERENCES discord_role (id)
);

# Create table for role requirements
CREATE TABLE carry_role_requirement
(
    id         BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    role_id    BIGINT                NOT NULL,
    type       TINYINT               NOT NULL,
    text_value VARCHAR               NOT NULL,
    value      BIGINT                NOT NULL,
    FOREIGN KEY (role_id) REFERENCES carry_role (id)
);