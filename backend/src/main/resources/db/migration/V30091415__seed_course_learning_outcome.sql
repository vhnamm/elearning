-- ==============================================================================
-- 1. LEARNING OUTCOMES & REQUIRED SKILLS (Khóa 1 và Khóa 4)
-- ==============================================================================
INSERT INTO course_learning_outcomes (course_id, content) VALUES
-- Khóa 1: Spring Boot
(1, 'Hiểu sâu nguyên lý IoC, Dependency Injection và Bean Lifecycle trong Spring'),
(1, 'Thiết kế RESTful API chuẩn chuẩn doanh nghiệp kèm validation và global exception'),
(1, 'Làm chủ Spring Data JPA, tối ưu câu truy vấn và xử lý quan hệ phức tạp'),
(1, 'Bảo mật ứng dụng toàn diện với Spring Security và Stateless JWT'),
-- Khóa 4: HeyGen AI
(4, 'Tạo Avatar AI và Clone giọng đọc tiếng Việt chân thực bằng HeyGen'),
(4, 'Tự động hóa kịch bản video viral đa kênh kết hợp ChatGPT'),
(4, 'Quy trình sản xuất hàng loạt video bán hàng không cần xuất hiện trước ống kính');

INSERT INTO course_required_skills (course_id, content) VALUES
-- Khóa 1: Spring Boot
(1, 'Nắm chắc kiến thức nền tảng Lập trình hướng đối tượng (OOP) với Java'),
(1, 'Biết sử dụng cơ bản SQL và hệ quản trị cơ sở dữ liệu MySQL'),
-- Khóa 4: HeyGen AI
(4, 'Sử dụng máy tính cơ bản, có tài khoản ChatGPT hoặc HeyGen'),
(4, 'Không yêu cầu kiến thức kỹ thuật hay kinh nghiệm dựng video trước đó');

