CREATE TABLE courses (
     id         BIGINT AUTO_INCREMENT,
     slug       VARCHAR(255) NOT NULL UNIQUE,
     instructor_id BIGINT NOT NULL,
     subcategory_id BIGINT NOT NULL,
     title      VARCHAR(255) NOT NULL,
     short_description  VARCHAR(500) NULL,
     description   TEXT  NULL,
     thumbnail_url   VARCHAR(1024)  NOT NULL,
     price          DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
     status         ENUM('DRAFT', 'PENDING_REVIEW', 'PUBLISHED', 'REJECTED') DEFAULT 'DRAFT',
     level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED'),
     created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP   NULL,
     PRIMARY KEY (id),
     CONSTRAINT fk_course_instructor
         FOREIGN KEY (instructor_id) REFERENCES users (id),
     CONSTRAINT fk_course_subcategory
        FOREIGN KEY (subcategory_id) REFERENCES subcategories (id)
) ENGINE=InnoDB;