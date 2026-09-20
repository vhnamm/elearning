-- Seed data cho trang "Tất cả khoá học của instructor"
-- instructor_id = 2 (usertest1@gmail.com, có cả role INSTRUCTOR + STUDENT, từ V2)
-- student_id    = 3, 4 (student1@gmail.com, student2@gmail.com, học viên thuần, từ V2)

-- 1. Courses với đủ các trạng thái để test filter tabs (DRAFT / PENDING_REVIEW / PUBLISHED / REJECTED)
INSERT INTO courses (
    id, slug, instructor_id, title,
    short_description, description,
    thumbnail_url, price, status, level
) VALUES
      (1, 'spring-boot-tu-co-ban-den-nang-cao', 2, 'Spring Boot từ cơ bản đến nâng cao',
       'Xây dựng REST API chuẩn doanh nghiệp với Spring Boot 3 và MySQL.',
       'Khóa học trang bị toàn diện kỹ năng Backend thực chiến từ số 0:

      - Phần 1: Tổng quan Spring Framework, nguyên lý IoC & Dependency Injection.
      - Phần 2: Thiết kế RESTful API chuẩn mực, xử lý Validation và Global Exception.
      - Phần 3: Thao tác dữ liệu với Spring Data JPA, Hibernate, xử lý quan hệ và tối ưu truy vấn.
      - Phần 4: Bảo mật ứng dụng với Spring Security, Stateless JWT và phân quyền RBAC.
      - Phần 5: Quản lý version database bằng Flyway, đóng gói Docker và deploy VPS.

      Yêu cầu đầu vào: Nắm chắc lập trình hướng đối tượng (OOP) với Java căn bản.',
       'https://pub-5ab381ad35034ad6b27959e4a6136657.r2.dev/courses/1/thumbnail/khoa_1_thumb_1.jpg', 1000.00, 'PUBLISHED', 'INTERMEDIATE'),

      (2, 'thiet-ke-ui-ux-chuyen-nghiep-voi-figma', 2, 'Thiết kế UI/UX chuyên nghiệp với Figma',
       'Làm chủ tư duy trải nghiệm người dùng, Design System và Prototype.',
       'Khóa học dẫn dắt bạn qua quy trình thiết kế sản phẩm số thực tế:

      - Chương 1: Nghiên cứu người dùng (User Persona, User Journey, Empathy Map).
      - Chương 2: Wireframing và kiến trúc thông tin (Information Architecture).
      - Chương 3: Sử dụng thành thạo Figma (Auto-layout, Components, Variants, Variables).
      - Chương 4: Xây dựng Design System đồng nhất theo chuẩn Atomic Design.
      - Chương 5: Tạo tương tác mượt mà với Advanced Prototyping và bàn giao dự án cho Developer.

      Dành cho: Người mới bắt đầu chuyển ngành hoặc lập trình viên muốn nâng cao mắt thẩm mỹ.',
       'https://placehold.co/600x400?text=Figma+UIUX', 10000.00, 'PUBLISHED', 'BEGINNER'),

      (3, 'thi-giac-may-tinh-voi-opencv-va-yolo', 2, 'Thị giác máy tính với OpenCV và YOLO',
       'Nhận diện đối tượng, phân tích luồng camera thời gian thực bằng Python.',
       'Nội dung khóa học đang trong quá trình biên soạn và hoàn thiện tài liệu lab:

      - Giới thiệu xử lý ảnh số: Không gian màu, lọc nhiễu, tách biên và contours.
      - Tiền xử lý dữ liệu ảnh và video thời gian thực với OpenCV.
      - Huấn luyện mô hình phát hiện đối tượng với YOLO trên tập dữ liệu tùy chỉnh.
      - Tối ưu hóa mô hình với ONNX Runtime để chạy mượt trên thiết bị CPU và Edge.

      Dự kiến phát hành vào quý tới với đầy đủ source code và dataset mẫu.',
       'https://pub-5ab381ad35034ad6b27959e4a6136657.r2.dev/courses/3/thumbnail/khoa3.webp', 2000.00, 'DRAFT', 'ADVANCED'),

      (4, 'san-xuat-video-ai-chuyen-nghiep-voi-heygen', 2, 'Sản xuất Video AI chuyên nghiệp với HeyGen',
       'Ứng dụng MC ảo, nhân bản giọng nói và tự động hóa sản xuất video bán hàng.',
       'Khóa học hướng dẫn quy trình tạo video marketing và bài giảng không cần quay mặt:

      - Module 1: Thiết lập Avatar tùy chỉnh (Custom Avatar) và Clone giọng nói tiếng Việt chuẩn cảm xúc.
      - Module 2: Viết kịch bản video viral với ChatGPT kết hợp kỹ thuật ngắt nghỉ câu cho HeyGen.
      - Module 3: Kỹ thuật phối nền, chèn b-roll, hiệu ứng chuyển cảnh và logo thương hiệu.
      - Module 4: Tự động hóa sản xuất video hàng loạt phục vụ chạy quảng cáo TikTok và Reels.',
       'https://pub-5ab381ad35034ad6b27959e4a6136657.r2.dev/courses/4/thumbnail/khoa4.webp', 1000.00, 'PENDING_REVIEW', 'INTERMEDIATE'),

      (5, 'chinh-phuc-ielts-writing-task-2-band-7', 2, 'Chinh phục IELTS Writing Task 2 Band 7.0+',
       'Nâng cấp tư duy phát triển luận điểm, từ vựng học thuật và liên kết câu.',
       'Khóa học tập trung giải quyết dứt điểm các lỗi mất điểm trong bài thi viết Task 2:

      - Phân tích và xử lý 5 dạng đề phổ biến: Opinion, Discussion, Problem-Solution, Advantages-Disadvantages và Two-Part Question.
      - Xây dựng hệ thống luận điểm logic, mạch lạc (Coherence and Cohesion).
      - Nâng cấp vốn từ vựng học thuật theo chủ đề (Lexical Resource) và cách dùng collocations tự nhiên.
      - Kỹ thuật đa dạng hóa cấu trúc ngữ pháp (Grammatical Range) và cách sửa các lỗi sai kinh điển.

      Mục tiêu đầu ra: Tự tin viết bài đạt band điểm từ 6.5 đến 7.5+ trong phòng thi.',
       'https://pub-5ab381ad35034ad6b27959e4a6136657.r2.dev/courses/5/thumbnail/khoa5.webp', 0.00, 'PUBLISHED', 'BEGINNER');;

-- 2. Enrollments (chỉ enroll vào course đã PUBLISHED) để tính studentsCount
INSERT IGNORE INTO enrollments
    (id, student_id, course_id, order_item_id, status, enrolled_at, completed_at)
VALUES
    (1, 3, 1, NULL, 'ACTIVE', '2026-08-01 09:00:00', NULL),
    (2, 4, 1, NULL, 'COMPLETED', '2026-08-03 09:00:00', '2026-09-01 14:00:00'),
    (3, 3, 2, NULL, 'COMPLETED', '2026-07-15 10:30:00', '2026-08-10 14:00:00');

-- 3. Course reviews (chỉ review course đã PUBLISHED) để tính avgRating
INSERT IGNORE INTO course_reviews
    (id, course_id, student_id, rating, comment, created_at, updated_at)
VALUES
    (1, 1, 3, 5, 'Khoá học rất chi tiết, dễ hiểu.', '2026-08-05 08:00:00', NULL),
    (2, 1, 4, 4, 'Nội dung hay nhưng hơi dài.', '2026-09-02 08:00:00', NULL),
    (3, 2, 3, 4, 'Nội dung tốt nhưng hơi nhanh với người mới.', '2026-08-20 08:00:00', NULL);
