-- ==============================================================================
-- 3. CURRICULUM ITEMS (LECTURE / QUIZ)
-- Khóa 1: IDs 1 -> 9
-- Khóa 4: IDs 10 -> 15
-- ==============================================================================
INSERT INTO curriculum_items (id, section_id, type, title, position, is_preview) VALUES
-- Khóa 1 - Section 1
(1,  1, 'LECTURE', 'Tổng quan khóa học và kiến trúc Spring Boot', 0, true),
(2,  1, 'LECTURE', 'Cài đặt JDK 21, IntelliJ IDEA và MySQL',      1, true),
(3,  1, 'LECTURE', 'Khởi tạo project chuẩn qua Spring Initializr', 2, false),

-- Khóa 1 - Section 2
(4,  2, 'LECTURE', 'Hiểu bản chất IoC & Dependency Injection',   0, false),
(5,  2, 'LECTURE', 'Xây dựng REST Controller chuẩn RESTful',     1, false),
(6,  2, 'QUIZ',    'Trắc nghiệm kiến thức Spring Core & REST',    2, false),

-- Khóa 1 - Section 3
(7,  2, 'LECTURE', 'Kết nối MySQL và cấu hình Spring Data JPA',  0, false),
(8,  3, 'LECTURE', 'Thực hành: Xây dựng CRUD API Hoàn chỉnh',     1, false),
(9,  3, 'QUIZ',    'Bài test đánh giá kỹ năng Backend',          2, false),

-- Khóa 4 - Section 4 (HeyGen AI)
(10, 4, 'LECTURE', 'Tổng quan về kỷ nguyên Video AI không cần quay mặt', 0, true),
(11, 4, 'LECTURE', 'Thiết lập tài khoản & Làm quen giao diện HeyGen',    1, false),
(12, 4, 'QUIZ',    'Kiểm tra khái niệm công cụ Video Generative AI',      2, false),

-- Khóa 4 - Section 5 (HeyGen AI)
(13, 5, 'LECTURE', 'Quy trình tạo Avatar ảo cử động môi khớp tiếng Việt', 0, false),
(14, 5, 'LECTURE', 'Kết hợp ChatGPT viết script viral và ghép nhạc nền', 1, false),
(15, 5, 'QUIZ',    'Trắc nghiệm quy trình xuất bản video tự động',       2, false);