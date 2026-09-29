CREATE TABLE quizzes (
     id              BIGINT NOT NULL AUTO_INCREMENT,
     curriculum_item_id BIGINT NOT NULL,
     passing_score   INT    NOT NULL DEFAULT 70,
     PRIMARY KEY (id),
     UNIQUE KEY uq_quizzes_content_item (content_item_id),
     CONSTRAINT fk_quizzes_item
         FOREIGN KEY (curriculum_item_id ) REFERENCES curriculum_items (id) ON DELETE CASCADE
) ENGINE=InnoDB;