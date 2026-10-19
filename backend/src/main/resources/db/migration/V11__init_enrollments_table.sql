CREATE TABLE enrollments (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id          BIGINT NOT NULL,
    course_id           BIGINT NOT NULL,
    baseline_version_id BIGINT NULL, -- version của khóa lúc mua (bất biến). TODO: siết NOT NULL khi code enroll ghi cột này
    order_item_id       BIGINT NULL, -- trace nguồn gốc mua, null nếu enroll miễn phí
    status              ENUM('ACTIVE', 'COMPLETED', 'REVOKED'),
    enrolled_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at        TIMESTAMP NULL,

    CONSTRAINT fk_enrollment_student
        FOREIGN KEY (student_id) REFERENCES users (id),
    CONSTRAINT fk_enrollment_course
        FOREIGN KEY (course_id) REFERENCES courses (id),
    CONSTRAINT fk_enrollment_baseline_version
        FOREIGN KEY (baseline_version_id) REFERENCES course_versions (id),
    CONSTRAINT uq_enrollment_student_course UNIQUE (student_id, course_id)
)ENGINE=InnoDB;

-- ==============================================================================
-- BẢN CŨ (giữ lại để đối chiếu, đã comment)
-- ==============================================================================
-- CREATE TABLE enrollments (
--     id      BIGINT AUTO_INCREMENT PRIMARY KEY,
--     student_id      BIGINT NOT NULL,
--     course_id       BIGINT NOT NULL,
--     order_item_id   BIGINT NULL, -- trace nguồn gốc mua, null nếu enroll miễn phí
--     status          ENUM('ACTIVE', 'COMPLETED', 'REVOKED'),
--     enrolled_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     completed_at    TIMESTAMP NULL,
--
--     CONSTRAINT fk_enrollment_student
--         FOREIGN KEY (student_id) REFERENCES users (id),
--     CONSTRAINT fk_enrollment_course
--         FOREIGN KEY (course_id) REFERENCES courses (id)
-- )ENGINE=InnoDB
