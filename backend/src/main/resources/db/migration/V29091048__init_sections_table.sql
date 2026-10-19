CREATE TABLE sections (
      id                BIGINT       NOT NULL AUTO_INCREMENT,
      course_version_id BIGINT       NOT NULL,
      title             VARCHAR(255) NOT NULL,
      position          INT          NOT NULL DEFAULT 0,
      created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      CONSTRAINT fk_sections_course_version
          FOREIGN KEY (course_version_id) REFERENCES course_versions (id) ON DELETE CASCADE,
      INDEX idx_sections_version_position (course_version_id, position)
) ENGINE=InnoDB;

-- ==============================================================================
-- BẢN CŨ (giữ lại để đối chiếu, đã comment)
-- ==============================================================================
-- CREATE TABLE sections (
--       id         BIGINT       NOT NULL AUTO_INCREMENT,
--       course_id  BIGINT       NOT NULL,
--       title      VARCHAR(255) NOT NULL,
--       position   INT          NOT NULL DEFAULT 0,
--       created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
--       PRIMARY KEY (id),
--       CONSTRAINT fk_sections_course
--           FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE,
--       INDEX idx_sections_course_position (course_id, position)
-- ) ENGINE=InnoDB;
