CREATE TABLE curriculum_items (
      id         BIGINT       NOT NULL AUTO_INCREMENT,
      section_id BIGINT       NOT NULL,
      type       ENUM('LECTURE', 'QUIZ') NOT NULL,
      title      VARCHAR(255) NOT NULL,
      position   INT          NOT NULL DEFAULT 0,
      is_preview BOOLEAN      NOT NULL DEFAULT FALSE,
      created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      CONSTRAINT fk_items_section
          FOREIGN KEY (section_id) REFERENCES sections (id) ON DELETE CASCADE
) ENGINE=InnoDB;