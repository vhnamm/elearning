INSERT INTO curriculum_items (id, section_id, type, title, position, is_preview) VALUES
-- Section 1: 2 bài xem trước, 1 bài khóa
(1, 1, 'LECTURE', 'Tổng quan khóa học',          0, true),
(2, 1, 'LECTURE', 'Cài đặt môi trường',          1, true),
(3, 1, 'LECTURE', 'Lộ trình học hiệu quả',       2, false),
-- Section 2
(4, 2, 'LECTURE', 'Khái niệm cốt lõi',           0, false),
(5, 2, 'LECTURE', 'Cấu trúc và cách hoạt động',  1, false),
(6, 2, 'QUIZ',    'Kiểm tra kiến thức nền tảng', 2, false),
-- Section 3
(7, 3, 'LECTURE', 'Xây dựng project mẫu',        0, false),
(8, 3, 'LECTURE', 'Tối ưu và best practices',    1, false),
(9, 3, 'QUIZ',    'Bài kiểm tra cuối khóa',      2, false);