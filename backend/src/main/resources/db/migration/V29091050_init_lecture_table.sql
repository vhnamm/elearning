CREATE TABLE lectures (
      id                     BIGINT        NOT NULL AUTO_INCREMENT,
      curriculum_item_id        BIGINT        NOT NULL,
      content_type           ENUM('VIDEO', 'FILE') NOT NULL,
      video_status           ENUM('UPLOADING', 'PROCESSING', 'READY', 'FAILED') NOT NULL DEFAULT 'UPLOADING',
      video_raw_key          VARCHAR(1024) NULL,
      video_key              VARCHAR(1024) NULL,
      video_duration_seconds INT           NULL,
      thumbnail_url          VARCHAR(1024)  NULL,
      PRIMARY KEY (id),
      UNIQUE KEY uq_lectures_content_item (content_item_id),
      CONSTRAINT fk_lectures_item
          FOREIGN KEY (curriculum_item_id) REFERENCES curriculum_items (id) ON DELETE CASCADE
) ENGINE=InnoDB;