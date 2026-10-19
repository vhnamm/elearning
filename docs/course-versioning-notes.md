# Course versioning: sửa khóa đã publish mà không ảnh hưởng học viên (2026-10-09)

Mục tiêu: giảng viên sửa được khóa đã PUBLISHED mà học viên đang học không bị gián đoạn. Nội dung sửa phải qua admin duyệt rồi mới thay bản live.

## Kiến trúc đã chốt
- `course_versions` chứa mọi nội dung học viên nhìn thấy và admin duyệt: title, short_description, description, thumbnail_key, level, category, subcategory, topics, outcomes, required skills, toàn bộ curriculum (sections -> curriculum_items).
- `courses` chỉ giữ: slug, price, instructor_id, status, `published_version_id` (bản đang live). Giá sửa thẳng, không cần duyệt.
- Học viên và trang public luôn đọc `courses.published_version_id`.
- Mỗi khóa tối đa **một bản mở** (DRAFT / PENDING_REVIEW / REJECTED). Tạo khóa lần đầu: Course + version 1 DRAFT, sửa thẳng. Khóa đã published: "Tạo bản nháp mới" clone cả cây sang version n+1 DRAFT.
- `course_versions.status`: DRAFT, PENDING_REVIEW, REJECTED, PUBLISHED, RETIRED.
- `curriculum_items.stable_key` (UUID): giữ nguyên giá trị khi clone, dùng để đồng bộ tiến độ học viên giữa các version (bảng progress lưu cùng giá trị, không FK).
- `enrollments.baseline_version_id`: version lúc mua, không đổi. Quy tắc tính hoàn thành (chỉ bài baseline hay mọi bài hiện tại) chưa chốt, làm ở bước học viên.
- Thumbnail lưu **key**, URL dựng bằng `StorageService.publicUrl(key)`.
- `course_versions.open_course_id`: cột sinh tự động (= course_id khi version đang mở, NULL khi không) + UNIQUE -> DB chặn có hai bản mở cho một khóa. Entity không map cột này.

## Đã sửa

| # | File | Thay đổi |
|---|------|----------|
| 1 | `db/migration` V7, V8, V9, V11, V15, V27092218/19, V29091048/49, V30091415/1600 | Sửa trực tiếp, bản cũ comment để đối chiếu. V24090454 gộp vào V7/V8 |
| 2 | `course/entity/*` | `Course` (slim), `CourseVersion` (mới), outcomes/skills/sections trỏ `courseVersion`, `CurriculumItem.stableKey`, `Enrollment.baselineVersion` |
| 3 | `course/repository/CourseRepository.java` | `@EntityGraph` qua `publishedVersion`; các query category/subcategory/title join `c.publishedVersion v`; bỏ `findWithDetailById`, `findCourseDetailById` |
| 4 | `course/repository/CourseVersionRepository.java` (mới) | `findFirstByCourseIdAndStatusIn`, `existsByIdAndCourseInstructorId`, `findWithDetailById`, `findCourseDetailById`, `findMaxVersionNo`, `findAll(spec, pageable)` |
| 5 | `course/specification/CourseSpecification.java` | Nội dung (title, keyword, category, subcategory, topic, level) đọc qua `version(root)` = join `publishedVersion` một lần. Bỏ phần của giảng viên |
| 6 | `course/specification/CourseVersionSpecification.java` (mới) | `forInstructor`: gốc `CourseVersion`, lọc `course.instructor.id`, bỏ RETIRED, lọc tab status, keyword theo title |
| 7 | `CourseManagementServiceImpl.getInstructedCourses` | Danh sách "Khóa của tôi" là danh sách **version** (mỗi version một card). Sort FE (`createdAt`, `price`) map sang `course.createdAt` / `course.price`, sort phụ `course.id` DESC + `versionNo` DESC để hai card cùng khóa đứng cạnh nhau |
| 8 | `InstructorCourseItemDto`, `CourseSearchRequest` | Thêm `versionId`, `versionNo`; `status` là `CourseVersionStatus` |
| 9 | `course/mapper/CourseMapper.java` | `toVersionEntity`, `toDraftResponse`, `toBasicInfoResponse`, `toCourseDetailDto` nhận `CourseVersion`; bỏ hai `toEntity(...)` trả `Course`. `thumbnailUrl` ignore, service tự set từ `thumbnailKey` |
| 10 | `CourseBasicInfoResponse`, `CourseDraftResponse` | Thêm `versionId`, `versionNo` (và `status`, `thumbnailKey` cho basic info) để FE điều hướng |

## Ảnh hưởng code cũ
- Các method repo/mapper cũ trả `Course` cho nội dung đã đổi sang `CourseVersion`; service nào gọi sẽ lỗi biên dịch cho tới khi sửa.
- `CourseSearchRequest.status` đổi sang `CourseVersionStatus` (chỉ màn giảng viên dùng; public search cố định PUBLISHED).
- URL quản lý khóa dạng `/course/:courseId/version/:versionId/manage`. Card dùng `id` + `versionId` từ `InstructorCourseItemDto`. Vì `versionId` đến từ client, server **bắt buộc** kiểm tra: version thuộc đúng `courseId`, khóa thuộc giảng viên, và version đang mở (DRAFT/REJECTED) mới được ghi; bản PUBLISHED/PENDING_REVIEW chỉ đọc, ghi trả 409.
- `hasTopicId` giờ join thật và `distinct`.

## FE đã sửa (route theo version)
- Route: `/instructor/course/:courseId/version/:versionId/manage/overview` (`routes.jsx`).
- `CourseCard`: link dùng `course.id` + `course.versionId`; badge hiện `trạng thái · v{versionNo}`; sửa lỗi fallback `STATUS_META.draft` -> `DRAFT`.
- `CreateCourseInitial`: sau khi tạo, điều hướng bằng `result.id` + `result.versionId`.
- `CreateCourseOverview`: đọc `versionId` từ URL, truyền vào `getCourseBasicInfo` và `getPresignedUrl`.
- `course.service.js`: `getCourseBasicInfo(courseId, versionId)` -> `GET /courses/{courseId}/versions/{versionId}/basic-info` (đã có ở backend); `getPresignedUrl(contentType, courseId)` -> `POST /courses/{courseId}/thumbnail/presigned-url` (không cần versionId: presign chỉ cấp chỗ upload, key mỗi lần một khác; ảnh chỉ gắn vào version khi PUT basic-info).

## Chưa làm
- Service còn lỗi biên dịch: `CourseManagementServiceImpl` (`createDraft`, `getCourseBasicInfo`, `updateCourseBasicInfo`, presign), `PublicCourseServiceImpl` (`toCardDto`, `getCourseDetail`).
- `createDraft` phải tạo Course + CourseVersion v1 DRAFT trong một transaction.
- `CourseBasicInfoUpdateRequest.thumbnailUrl` -> `thumbnailKey`; presigned key nên chứa versionId/UUID để bản nháp không ghi đè ảnh live.
- `CourseModerationLog` thêm `courseVersion`; siết `NOT NULL` cho `course_version_id`, `baseline_version_id`.
- API clone/xóa nháp/gửi duyệt/rút lại, admin duyệt; logic publish (set `published_version_id`, version cũ -> RETIRED); đơn giản hóa `courses.status` (đang trùng với version).
- Bước học viên: `curriculum_item_progress` (stable_key), certificates (+`course_version_id`), quiz, quy tắc hoàn thành.
- FE: nút Sửa ở card PUBLISHED phải tạo/mở bản nháp (hiện vẫn link thẳng vào version của card).
- Danh sách giảng viên phân trang theo version nên một cặp published/draft có thể rơi vào hai trang liền nhau (chấp nhận).

## Cần kiểm tra
- Migration chưa chạy thật (chưa có MySQL/Docker trong phiên): reset DB, chạy app, xem Flyway và `ddl-auto: validate`. Nếu MySQL không chấp nhận cột `open_course_id` VIRTUAL kèm UNIQUE thì đổi thành `STORED`.
- Seed V15 có lỗi cũ: enrollment status `in_progress`/`completed` không thuộc enum (chưa đụng tới).
- `mvn compile`: `CourseMapper` đã qua; còn lỗi ở hai service như trên.
