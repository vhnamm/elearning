CREATE TABLE users (
   id         BIGINT AUTO_INCREMENT,
   email      VARCHAR(50) NOT NULL,
   password   VARCHAR(255) NULL,
   full_name   VARCHAR(50) NOT NULL,
   avatar     VARCHAR(255),
   google_id VARCHAR(50),
   created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
   deleted_at TIMESTAMP    NULL,

   PRIMARY KEY (id),
   UNIQUE KEY uk_users_email (email),
) ENGINE=InnoDB;


CREATE TABLE roles (
   id          INT AUTO_INCREMENT,
   `name`   VARCHAR(50)  NOT NULL,
   description TEXT         NULL,
   PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE user_roles (
    id              BIGINT AUTO_INCREMENT,
    user_id         BIGINT,
    role_id         INT,
    PRIMARY KEY (id),
    CONSTRAINT fk_user_role_1
      FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_user_role_2
     FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE refresh_tokens (
    token_hash VARCHAR(255),
    user_id BIGINT,
    client_ip VARCHAR(255),
    user_agent VARCHAR(255),
    expired_at TIMESTAMP,
    PRIMARY KEY (token_hash),
    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;


