CREATE TABLE course_moderation_logs (
    id            BIGINT  AUTO_INCREMENT PRIMARY KEY,
    course_id     BIGINT  NOT NULL,
    moderator_id  BIGINT  NOT NULL, -- ID của Admin/Moderator thực hiện duyệt
    action        ENUM('APPROVED', 'REJECTED') NOT NULL,
    feedback      TEXT NOT NULL,            -- Lý do từ chối hoặc ghi chú khi duyệt
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_moderation_course
        FOREIGN KEY (course_id) REFERENCES courses (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_moderation_moderator
        FOREIGN KEY (moderator_id) REFERENCES users (id)
) ENGINE=InnoDB;