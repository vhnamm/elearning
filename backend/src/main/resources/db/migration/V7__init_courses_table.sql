-- courses chỉ giữ thông tin ổn định / thương mại.
-- Nội dung hiển thị công khai (title, mô tả, thumbnail, level, category, subcategory, topics)
-- nằm ở course_versions (V8) và đi qua kiểm duyệt.
-- published_version_id trỏ tới version đang live; FK được thêm ở V8 (course_versions chưa tồn tại ở đây).
CREATE TABLE courses (
     id                   BIGINT AUTO_INCREMENT,
     slug                 VARCHAR(255)   NULL UNIQUE,
     instructor_id        BIGINT         NOT NULL,
     -- NULL = chưa đặt giá (bản nháp vừa tạo); V24090454 cũ đã MODIFY cột này thành NULL, khi gộp phải giữ lại
     price                DECIMAL(10, 2) NULL DEFAULT 0.00,
     status               ENUM('DRAFT', 'PENDING_REVIEW', 'PUBLISHED', 'REJECTED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT',
     published_version_id BIGINT         NULL,
     created_at           TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at           TIMESTAMP      NULL,
     PRIMARY KEY (id),
     CONSTRAINT fk_course_instructor
         FOREIGN KEY (instructor_id) REFERENCES users (id)
) ENGINE=InnoDB;

-- ==============================================================================
-- BẢN CŨ (giữ lại để đối chiếu, đã comment)
-- ==============================================================================
-- CREATE TABLE courses (
--      id         BIGINT AUTO_INCREMENT,
--      slug       VARCHAR(255) NOT NULL UNIQUE,
--      instructor_id BIGINT NOT NULL,
--      subcategory_id BIGINT NOT NULL,
--      title      VARCHAR(255) NOT NULL,
--      short_description  VARCHAR(500) NULL,
--      description   TEXT  NULL,
--      thumbnail_url   VARCHAR(1024)  NOT NULL,
--      price          DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
--      status         ENUM('DRAFT', 'PENDING_REVIEW', 'PUBLISHED', 'REJECTED') DEFAULT 'DRAFT',
--      level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED'),
--      created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
--      updated_at TIMESTAMP   NULL,
--      PRIMARY KEY (id),
--      CONSTRAINT fk_course_instructor
--          FOREIGN KEY (instructor_id) REFERENCES users (id),
--      CONSTRAINT fk_course_subcategory
--         FOREIGN KEY (subcategory_id) REFERENCES subcategories (id)
-- ) ENGINE=InnoDB;
