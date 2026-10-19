CREATE TABLE curriculum_items (
      id         BIGINT       NOT NULL AUTO_INCREMENT,
      section_id BIGINT       NOT NULL,
      -- Danh tính của bài học xuyên các version: clone sang version mới thì COPY NGUYÊN giá trị này.
      -- Tiến độ học viên (curriculum_item_progress) khớp theo stable_key, không theo id.
      stable_key CHAR(36)     NOT NULL DEFAULT (UUID()),
      type       ENUM('LECTURE', 'QUIZ') NOT NULL,
      title      VARCHAR(255) NOT NULL,
      position   INT          NOT NULL DEFAULT 0,
      is_preview BOOLEAN      NOT NULL DEFAULT FALSE,
      created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      INDEX idx_items_stable_key (stable_key),
      CONSTRAINT fk_items_section
          FOREIGN KEY (section_id) REFERENCES sections (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ==============================================================================
-- BẢN CŨ (giữ lại để đối chiếu, đã comment)
-- ==============================================================================
-- CREATE TABLE curriculum_items (
--       id         BIGINT       NOT NULL AUTO_INCREMENT,
--       section_id BIGINT       NOT NULL,
--       type       ENUM('LECTURE', 'QUIZ') NOT NULL,
--       title      VARCHAR(255) NOT NULL,
--       position   INT          NOT NULL DEFAULT 0,
--       is_preview BOOLEAN      NOT NULL DEFAULT FALSE,
--       created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
--       PRIMARY KEY (id),
--       CONSTRAINT fk_items_section
--           FOREIGN KEY (section_id) REFERENCES sections (id) ON DELETE CASCADE
-- ) ENGINE=InnoDB;
