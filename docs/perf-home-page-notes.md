# Tối ưu truy vấn trang chủ (2026-10-04)

Mục tiêu: giảm số câu SQL khi vào trang chủ (log ban đầu: 17 câu).

## Đã sửa

| # | File | Thay đổi |
|---|------|----------|
| 1 | `course/repository/CourseRepository.java` | Thêm `@EntityGraph({"instructor","subcategory"})` cho `findAll(spec, pageable)` và `findPopularCourses` -> hết N+1 instructor/subcategory |
| 2 | `review/repository/CourseReviewRepository.java` | Thêm `getRatingSummariesByCourseIds` (avg + count trong 1 câu) |
| 2 | `review/service/CourseReviewService.java` + `impl/CourseReviewImpl.java` | Thêm `getRatingSummariesByCourseIds` và record `RatingSummary` |
| 2 | `course/service/impl/PublicCourseServiceImpl.java` | `toCardDtos` dùng method mới (1 câu thay vì 2) |
| 3 | `security/jwt/JwtFilter.java` | `shouldNotFilter` bỏ qua GET `/courses`, `/courses/popular`, `/courses/{id}`, `/courses/{id}/curriculum`, `/search/**`, `/categories/**` -> không query user |

## Ảnh hưởng code cũ
- Đã **xoá** `getAverageRatingsByCourseIds` / `getReviewCountsByCourseIds` (service + repository). Các chỗ gọi cũ (`PublicCourseServiceImpl.getCourseDetail`, `CourseManagementServiceImpl`) đã chuyển sang `getRatingSummariesByCourseIds`. Hành vi giữ nguyên: instructor list vẫn trả `rating = null` nếu khóa không có trong map.
- `@EntityGraph` trên `findAll(spec, pageable)` áp dụng cho mọi nơi gọi, kể cả list khóa học của instructor (`CourseManagementServiceImpl`): chỉ thêm 2 LEFT JOIN `@ManyToOne`, kết quả không đổi; count query không bị ảnh hưởng.
- JwtFilter: các endpoint public ở trên giờ luôn là anonymous, dù FE gửi token. Hiện các controller này không đọc principal nên không ảnh hưởng. **Nếu sau này các endpoint này cần biết user (vd. "đã enroll chưa"), phải bỏ chúng khỏi `PUBLIC_GET_PATHS`.**
- Cố ý KHÔNG skip `/courses/related-queries` và `/topics/**` vì chúng chưa nằm trong `permitAll` của `SecurityConfig` (hiện đang yêu cầu đăng nhập).

## Chưa làm
- Cache `UserDetails` cho request cần auth (`/auth/me`...).
- Denormalize `enrollment_count`/`avg_rating`/`review_count` vào `courses` để bỏ correlated subquery khi sort popular.
- Cân nhắc thêm `/api/v1/topics/**` và `/courses/related-queries` vào `permitAll` nếu muốn public (cần xác nhận).

## Cần kiểm tra
- `mvn compile` đã pass; chưa chạy app/test. Chạy lại trang chủ, xem log Hibernate: không còn select `users`/`subcategories` lẻ, chỉ còn 1 câu rating+count.
