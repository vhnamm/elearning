CREATE TABLE course_reviews(
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    rating      TINYINT UNSIGNED NOT NULL,
    comment     VARCHAR(500) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,

    CONSTRAINT fk_course_review_course_id
        FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_course_review_student_id
        FOREIGN KEY (student_id) REFERENCES users(id)
)ENGINE=InnoDB;