-- Seed v1 của từng khóa: course_version_id = course_id (V15 seed version với id trùng id khóa)
-- OLD: INSERT INTO sections (id, course_id, title, position) VALUES
INSERT INTO sections (id, course_version_id, title, position) VALUES
-- Khóa 1: Spring Boot (PUBLISHED)
(1, 1, 'Giới thiệu & Cài đặt môi trường', 0),
(2, 1, 'Spring Boot Core & REST API',     1),
(3, 1, 'Spring Data JPA & Thực hành',     2),

-- Khóa 4: HeyGen AI (PENDING_REVIEW)
(4, 4, 'Khởi động với Video AI & HeyGen', 0),
(5, 4, 'Sản xuất Video Marketing thực chiến', 1);