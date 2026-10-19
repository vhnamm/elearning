-- Mỗi lần giảng viên cập nhật khóa đã published sẽ có 1 version mới (clone từ bản published).
-- Học viên luôn đọc version = courses.published_version_id.
CREATE TABLE course_versions (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    course_id         BIGINT        NOT NULL,
    version_no        INT           NOT NULL,
    status            ENUM('DRAFT', 'PENDING_REVIEW', 'REJECTED', 'PUBLISHED', 'RETIRED') NOT NULL DEFAULT 'DRAFT',

    -- Nội dung hiển thị công khai (bước 1 của wizard) -> qua duyệt
    title             VARCHAR(255)  NULL,
    short_description VARCHAR(500)  NULL,
    description       TEXT          NULL,
    thumbnail_key    VARCHAR(1024) NULL,
    level             ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') NULL,
    category_id       BIGINT        NULL,
    subcategory_id    BIGINT        NULL,

    submitted_at      TIMESTAMP     NULL,
    published_at      TIMESTAMP     NULL,
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP     NULL,

    -- Mỗi khóa chỉ có tối đa 1 bản đang mở (DRAFT / PENDING_REVIEW / REJECTED).
    -- UNIQUE bỏ qua NULL nên các bản PUBLISHED / RETIRED không bị ràng buộc.
    open_course_id    BIGINT GENERATED ALWAYS AS (
        CASE WHEN status IN ('DRAFT', 'PENDING_REVIEW', 'REJECTED') THEN course_id ELSE NULL END
    ) VIRTUAL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_course_version_no (course_id, version_no),
    UNIQUE KEY uq_course_open_version (open_course_id),
    CONSTRAINT fk_cv_course      FOREIGN KEY (course_id)      REFERENCES courses (id),
    CONSTRAINT fk_cv_category    FOREIGN KEY (category_id)    REFERENCES categories (id),
    CONSTRAINT fk_cv_subcategory FOREIGN KEY (subcategory_id) REFERENCES subcategories (id)
) ENGINE=InnoDB;

-- courses -> version đang live (FK vòng nên thêm sau khi course_versions đã tồn tại)
ALTER TABLE courses
    ADD CONSTRAINT fk_course_published_version
        FOREIGN KEY (published_version_id) REFERENCES course_versions (id);

-- Thay cho course_topics: topic gắn theo version (draft sửa topic không ảnh hưởng bản live)
CREATE TABLE course_version_topics (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_version_id BIGINT NOT NULL,
    topic_id          BIGINT NOT NULL,

    CONSTRAINT uq_cvt_version_topic UNIQUE (course_version_id, topic_id),

    CONSTRAINT fk_cvt_version
        FOREIGN KEY (course_version_id) REFERENCES course_versions (id)
            ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_cvt_topic
        FOREIGN KEY (topic_id) REFERENCES topics (id)
            ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB;

-- ==============================================================================
-- BẢN CŨ (giữ lại để đối chiếu, đã comment)
-- ==============================================================================
--
-- CREATE TABLE course_topics (
--    id BIGINT AUTO_INCREMENT PRIMARY KEY,
--    course_id BIGINT NOT NULL,
--    topic_id BIGINT NOT NULL,
--
-- -- Ràng buộc UNIQUE để chống trùng lặp (1 khóa học không thể gắn 1 topic 2 lần)
--    CONSTRAINT uq_course_topics_course_topic UNIQUE (course_id, topic_id),
--
-- -- Khóa ngoại tới bảng courses
--    CONSTRAINT fk_course_topics_course_id
--        FOREIGN KEY (course_id) REFERENCES courses (id)
--            ON DELETE CASCADE ON UPDATE CASCADE,
--
-- -- Khóa ngoại tới bảng topics
--    CONSTRAINT fk_course_topics_topic_id
--        FOREIGN KEY (topic_id) REFERENCES topics (id)
--            ON DELETE RESTRICT ON UPDATE CASCADE
-- ) ENGINE = InnoDB;
