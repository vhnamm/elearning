CREATE TABLE enrollments (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id      BIGINT NOT NULL,
    course_id       BIGINT NOT NULL,
    order_item_id   BIGINT NULL, -- trace nguồn gốc mua, null nếu enroll miễn phí
    status          ENUM('ACTIVE', 'COMPLETED', 'REVOKED'),
    enrolled_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at    TIMESTAMP NULL,

    CONSTRAINT fk_enrollment_student
        FOREIGN KEY (student_id) REFERENCES users (id),
    CONSTRAINT fk_enrollment_course
        FOREIGN KEY (course_id) REFERENCES courses (id)
)ENGINE=InnoDB