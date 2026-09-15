CREATE TABLE instructor_profiles (
   id         BIGINT AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
   experience      VARCHAR(50) NULL,
   headline  VARCHAR(255) NULL,
   bio   TEXT  NULL,
   joined_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (id),
   CONSTRAINT fk_user_profile_user
       FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;