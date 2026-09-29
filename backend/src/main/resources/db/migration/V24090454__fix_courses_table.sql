ALTER TABLE courses
    ADD     COLUMN category_id       BIGINT,
    MODIFY COLUMN slug               VARCHAR(255)   NULL,
    MODIFY COLUMN instructor_id      BIGINT         NULL,
    MODIFY COLUMN subcategory_id     BIGINT         NULL,
    MODIFY COLUMN title              VARCHAR(255)   NULL,
    MODIFY COLUMN thumbnail_url      VARCHAR(1024)  NULL,
    MODIFY COLUMN price              DECIMAL(10, 2) NULL DEFAULT 0.00,
    MODIFY COLUMN created_at         TIMESTAMP      NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE courses
    ADD CONSTRAINT fk_course_category
        FOREIGN KEY (category_id) REFERENCES categories (id);
