-- ==============================================================================
-- 1. SEED CATEGORIES (Đúng 10 Danh mục lớn chuẩn Udemy)
-- ==============================================================================
INSERT INTO categories (id, name, slug, created_at) VALUES
    (1,  'Lập trình & Phát triển',         'lap-trinh-va-phat-trien',         NOW()),
    (2,  'CNTT & Phần mềm',                'cntt-va-phan-mem',                NOW()),
    (3,  'Thiết kế & Đồ họa',              'thiet-ke-va-do-hoa',              NOW()),
    (4,  'Trí tuệ nhân tạo & Data',        'tri-tue-nhan-tao-va-data',        NOW()),
    (5,  'Marketing & Truyền thông số',    'marketing-va-truyen-thong-so',    NOW()),
    (6,  'Kinh doanh & Khởi nghiệp',       'kinh-doanh-va-khoi-nghiep',       NOW()),
    (7,  'Ngoại ngữ',                      'ngoai-ngu',                       NOW()),
    (8,  'Nhiếp ảnh & Video',              'nhiep-anh-va-video',              NOW()),
    (9,  'Phát triển Bản thân',            'phat-trien-ban-than',             NOW()),
    (10, 'Âm nhạc & Nghệ thuật',           'am-nhac-va-nghe-thuat',           NOW());


-- ==============================================================================
-- 2. SEED SUBCATEGORIES (Mỗi Category đúng 3 Subcategories = 30 Subcategories)
-- ==============================================================================
INSERT INTO subcategories (id, category_id, name, slug, created_at) VALUES
-- Category 1: Lập trình & Phát triển
(1,  1, 'Phát triển Web',               'phat-trien-web',                  NOW()),
(2,  1, 'Lập trình Di động',            'lap-trinh-di-dong',               NOW()),
(3,  1, 'Ngôn ngữ Lập trình',           'ngon-ngu-lap-trinh',              NOW()),

-- Category 2: CNTT & Phần mềm
(4,  2, 'Mạng & An toàn thông tin',     'mang-va-an-toan-thong-tin',       NOW()),
(5,  2, 'Hệ điều hành & Máy chủ',       'he-dieu-hanh-va-may-chu',         NOW()),
(6,  2, 'Điện toán Đám mây & DevOps',   'dien-toan-dam-may-va-devops',     NOW()),

-- Category 3: Thiết kế & Đồ họa
(7,  3, 'Thiết kế Giao diện UI/UX',     'thiet-ke-giao-dien-ui-ux',        NOW()),
(8,  3, 'Đồ họa Minh họa',              'do-hoa-minh-hoa',                 NOW()),
(9,  3, 'Thiết kế 3D & Diễn họa',       'thiet-ke-3d-va-dien-hoa',         NOW()),

-- Category 4: Trí tuệ nhân tạo & Data
(10, 4, 'Thị giác Máy tính',       'thi-giac-may-tinh-cv',            NOW()),
(11, 4, 'Khoa học Dữ liệu & ML',        'khoa-hoc-du-lieu-va-ml',          NOW()),
(12, 4, 'Công cụ AI Sáng tạo',          'cong-cu-ai-sang-tao',             NOW()),

-- Category 5: Marketing & Truyền thông số
(13, 5, 'Digital Marketing & Quảng cáo', 'digital-marketing-va-quang-cao', NOW()),
(14, 5, 'Content & Copywriting',        'content-va-copywriting',          NOW()),
(15, 5, 'Social Media Marketing',       'social-media-marketing',          NOW()),

-- Category 6: Kinh doanh & Khởi nghiệp
(16, 6, 'Quản trị Dự án & Agile',       'quan-tri-du-an-va-agile',         NOW()),
(17, 6, 'Tài chính & Kế toán',          'tai-chinh-va-ke-toan',            NOW()),
(18, 6, 'Khởi nghiệp & Chiến lược',     'khoi-nghiep-va-chien-luoc',       NOW()),

-- Category 7: Ngoại ngữ
(19, 7, 'Luyện thi Tiếng Anh (IELTS/TOEIC)', 'luyen-thi-tieng-anh',        NOW()),
(20, 7, 'Tiếng Anh Giao tiếp & Đi làm', 'tieng-anh-giao-tiep-di-lam',      NOW()),
(21, 7, 'Ngôn ngữ Châu Á', 'ngon-ngu-chau-a',             NOW()),

-- Category 8: Nhiếp ảnh & Video
(22, 8, 'Dựng phim & Biên tập Video',   'dung-phim-va-bien-tap-video',     NOW()),
(23, 8, 'Nhiếp ảnh Thương mại',         'nhiep-anh-thuong-mai',            NOW()),
(24, 8, 'Kỹ xảo Hình ảnh (VFX)',        'ky-xao-hinh-anh-vfx',             NOW()),

-- Category 9: Phát triển Bản thân
(25, 9, 'Kỹ năng Giao tiếp & Thuyết trình', 'ky-nang-giao-tiep-thuyet-trinh', NOW()),
(26, 9, 'Quản lý Thời gian & Năng suất', 'quan-ly-thoi-gian-nang-suat',    NOW()),
(27, 9, 'Tư duy Phản biện & Giải quyết Vấn đề', 'tu-duy-phan-bien',        NOW()),

-- Category 10: Âm nhạc & Nghệ thuật
(28, 10, 'Sản xuất Âm nhạc & Thu âm',   'san-xuat-am-nhac-va-thu-am',      NOW()),
(29, 10, 'Nhạc cụ',      'nhac-cu',                         NOW()),
(30, 10, 'Thanh nhạc & Luyện giọng',    'thanh-nhac-va-luyen-giong',       NOW());


-- ==============================================================================
-- 3. SEED TOPICS (Các Topic tiêu biểu, bao quát toàn bộ các khóa học mẫu)
-- ==============================================================================
INSERT INTO topics (id, subcategory_id, name, slug, status, created_at) VALUES
-- Topics cho Sub 1 (Phát triển Web)
(1,  1,  'Spring Boot',            'spring-boot',            'approved', NOW()),
(2,  1,  'React.js',               'react-js',               'approved', NOW()),
(3,  1,  'Node.js',                'node-js',                'approved', NOW()),

-- Topics cho Sub 2 (Di động)
(4,  2,  'Flutter',                'flutter',                'approved', NOW()),
(5,  2,  'Android với Kotlin',     'android-voi-kotlin',     'approved', NOW()),

-- Topics cho Sub 3 (Ngôn ngữ Lập trình)
(6,  3,  'Java',                   'java',                   'approved', NOW()),
(7,  3,  'Python',                 'python',                 'approved', NOW()),
(8,  3,  'C++',                    'cpp',                    'approved', NOW()),

-- Topics cho Sub 4, 5, 6 (CNTT & Phần mềm)
(9,  4,  'Bảo mật Mạng (Security+)', 'security-plus',        'approved', NOW()),
(10, 5,  'Quản trị Linux',         'quan-tri-linux',         'approved', NOW()),
(11, 6,  'Docker & Kubernetes',    'docker-va-kubernetes',   'approved', NOW()),

-- Topics cho Sub 7 (UI/UX)
(12, 7,  'Figma',                  'figma',                  'approved', NOW()),
(13, 7,  'Design System',          'design-system',          'approved', NOW()),
(14, 7,  'Thiết kế Wireframe',     'wireframe',              'approved', NOW()),

-- Topics cho Sub 8, 9 (Thiết kế đồ họa, 3D)
(15, 8,  'Adobe Photoshop',        'adobe-photoshop',        'approved', NOW()),
(16, 9,  'Blender 3D',             'blender-3d',             'approved', NOW()),

-- Topics cho Sub 10 (Thị giác Máy tính)
(17, 10, 'OpenCV',                 'opencv',                 'approved', NOW()),
(18, 10, 'YOLO Object Detection',  'yolo-object-detection',  'approved', NOW()),
(19, 10, 'Xử lý Ảnh số',          'xu-ly-anh-so',           'approved', NOW()),

-- Topics cho Sub 11 (Data & ML)
(20, 11, 'Machine Learning',       'machine-learning',       'approved', NOW()),
(21, 11, 'Deep Learning',          'deep-learning',          'approved', NOW()),

-- Topics cho Sub 12 (Công cụ AI Sáng tạo)
(22, 12, 'HeyGen Video AI',        'heygen-video-ai',        'approved', NOW()),
(23, 12, 'ChatGPT & Prompt Eng',   'chatgpt-prompt',         'approved', NOW()),
(24, 12, 'Midjourney',             'midjourney',             'approved', NOW()),

-- Topics cho Sub 13, 14, 15 (Marketing)
(25, 13, 'Facebook Ads & Google Ads', 'facebook-google-ads', 'approved', NOW()),
(26, 14, 'SEO Tổng thể',           'seo-tong-the',           'approved', NOW()),
(27, 15, 'TikTok Marketing',       'tiktok-marketing',       'approved', NOW()),

-- Topics cho Sub 16, 17, 18 (Kinh doanh)
(28, 16, 'Agile & Scrum',          'agile-va-scrum',         'approved', NOW()),
(29, 17, 'Phân tích Báo cáo Tài chính', 'tai-chinh-doanh-nghiep', 'approved', NOW()),
(30, 18, 'Mô hình Kinh doanh Lean', 'lean-startup',          'approved', NOW()),

-- Topics cho Sub 19, 20 (Ngoại ngữ)
(31, 19, 'IELTS Writing Task 2',   'ielts-writing-task-2',   'approved', NOW()),
(32, 19, 'IELTS Overall',          'ielts-overall',          'approved', NOW()),
(33, 20, 'Tiếng Anh Phỏng vấn',    'tieng-anh-phong-van',    'approved', NOW()),

-- Topics cho Sub 22 (Video)
(34, 22, 'Adobe Premiere Pro',     'adobe-premiere-pro',     'approved', NOW()),
(35, 22, 'CapCut Video Editing',   'capcut-video-editing',   'approved', NOW());


-- 2. Gắn tags chủ đề (Topics) vào từng khoá học qua bảng course_topics
-- Áp dụng cho bảng có PK (course_id, topic_id) hoặc tự tăng id:
INSERT IGNORE INTO course_topics (course_id, topic_id) VALUES
    -- Khóa 1 (Spring Boot) gắn tag: Spring Boot (1), Java (6)
    (1, 1),
    (1, 6),

    -- Khóa 2 (Figma UI/UX) gắn tag: Figma (12), Design System (13)
    (2, 12),
    (2, 13),

    -- Khóa 3 (OpenCV YOLO) gắn tag: OpenCV (17), YOLO Object Detection (18), Python (7)
    (3, 17),
    (3, 18),
    (3, 7),

    -- Khóa 4 (HeyGen Video AI) gắn tag: HeyGen Video AI (22), ChatGPT & Prompt Eng (23)
    (4, 22),
    (4, 23),

    -- Khóa 5 (IELTS) gắn tag: IELTS Writing Task 2 (31), IELTS Overall (32)
    (5, 31),
    (5, 32);
