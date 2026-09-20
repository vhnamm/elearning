-- 1. Insert Roles (bỏ qua nếu ID hoặc Name đã tồn tại)
INSERT IGNORE INTO roles (id, `name`, description) VALUES
(1, 'ADMIN', 'Quản trị viên hệ thống'),
(2, 'INSTRUCTOR', 'Giảng viên'),
(3, 'STUDENT', 'Học viên');

-- 2. Insert Users (bỏ qua nếu ID hoặc email đã tồn tại nhờ uk_users_email)
INSERT IGNORE INTO users (id, email, password, full_name, avatar, locked) VALUES
(1, 'admin@gmail.com', '$2a$10$Qt2TFd.LpJFfQHP1ohjNL.gE1Nm1eraZXyqpWTMFYkdKIBgvmPkIu', 'Admin', 'https://placehold.co/150', FALSE),
(2, 'usertest1@gmail.com', '$2a$10$Qt2TFd.LpJFfQHP1ohjNL.gE1Nm1eraZXyqpWTMFYkdKIBgvmPkIu', 'Thi Pride', 'https://placehold.co/150', FALSE),
(3, 'student1@gmail.com', '$2a$10$Qt2TFd.LpJFfQHP1ohjNL.gE1Nm1eraZXyqpWTMFYkdKIBgvmPkIu', 'Nguyen Van Hoc', 'https://placehold.co/150', FALSE),
(4, 'student2@gmail.com', '$2a$10$Qt2TFd.LpJFfQHP1ohjNL.gE1Nm1eraZXyqpWTMFYkdKIBgvmPkIu', 'Tran Thi Vien', 'https://placehold.co/150', FALSE);

-- 3. Insert User Roles
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@gmail.com' AND r.name = 'ADMIN'
UNION ALL
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'usertest1@gmail.com' AND r.name IN ('INSTRUCTOR', 'STUDENT')
UNION ALL
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email IN ('student1@gmail.com', 'student2@gmail.com') AND r.name = 'STUDENT';