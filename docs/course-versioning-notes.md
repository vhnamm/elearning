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


---

# Bước 3: Trang "Cài đặt giá & Khuyến mãi" (2026-11-10)

Hướng dẫn chuyển từ bước Đề cương sang trang giá, và code cả BE lẫn FE cho trang này. Tài liệu này mới là **hướng dẫn**, code chưa được thêm vào dự án.

## 1. Quyết định đã chốt

- Giá nằm ở `courses.price` (đã chốt ở phần trên): đổi giá **có hiệu lực ngay**, không qua duyệt, và **không phụ thuộc trạng thái version**. Vì vậy API giá chỉ cần `courseId`, không cần `versionId`.
- `courses.price` là `NULL` nghĩa là **chưa đặt giá** (bản nháp vừa tạo), `0` nghĩa là **miễn phí**.
- Khuyến mãi: mỗi khóa tối đa **một** khuyến mãi, giảm theo **phần trăm**, có thời gian bắt đầu và kết thúc. Trạng thái (sắp diễn ra / đang chạy / đã kết thúc) **tính lúc đọc** từ thời gian, không cần job chạy nền.
- Khóa miễn phí hoặc chưa đặt giá thì **không được** có khuyến mãi. Đổi về miễn phí thì khuyến mãi bị xóa theo.
- Giới hạn giá do BE quyết định và trả về cho FE (`minPaidPrice`, `maxPrice`), để FE không phải viết cứng.

## 2. Luồng chuyển sang trang giá

Có 3 cách vào trang, đều dẫn tới cùng một URL:

```
/instructor/course/:courseId/version/:versionId/manage/pricing
```

1. Bấm bước "Cài đặt giá & Khuyến mãi" ở sidebar.
2. Bấm nút "Tiếp tục" ở cuối trang Đề cương (thay cho nút "Lưu nháp" đang giả).
3. Gõ trực tiếp URL.

Việc cần làm ở FE để chuyển được:

| # | File | Việc |
|---|---|---|
| 1 | `routes.jsx` | Thêm route con `pricing` trong `.../manage` |
| 2 | `CreateCourseSidebar.jsx` | Đổi `ready: true` cho bước `pricing` |
| 3 | `CreateCourseCurriculum.jsx` | Đổi nút "Lưu nháp" thành "Tiếp tục", điều hướng sang `pricing` |
| 4 | `CreateCoursePricing.jsx` (mới) + `.module.scss` | Trang giá |
| 5 | `course.service.js` | 4 hàm gọi API |

## 3. Trang sẽ có những gì

```
┌ Giá khóa học ───────────────────────────────────────────┐
│  ( ) Miễn phí     (•) Có phí                             │
│  Giá bán: [ 499.000 ₫ ]   Từ 50.000 ₫ đến 50.000.000 ₫   │
│                                         [ Lưu giá ]      │
└──────────────────────────────────────────────────────────┘
┌ Khuyến mãi ─────────────────────────────── [Đang chạy] ──┐
│  Giảm: [ 20 ] %                                          │
│  Thời gian: [ 10/11/2026 00:00 ] → [ 17/11/2026 23:59 ]  │
│  Giá sau giảm: 399.200 ₫                                 │
│                          [ Xóa khuyến mãi ] [ Lưu ]      │
└──────────────────────────────────────────────────────────┘
```

- Thẻ **Giá**: chọn miễn phí hoặc có phí, nhập số tiền, lưu.
- Thẻ **Khuyến mãi**: chỉ dùng được khi đã lưu giá có phí. Nếu khóa miễn phí hoặc chưa có giá thì hiện thông báo và khóa các ô.
- Hiện thẻ trạng thái của khuyến mãi (`SCHEDULED`, `ACTIVE`, `EXPIRED`) và giá sau giảm.

## 4. BE

### 4.1. Migration

Số phiên bản **phải lớn hơn** bản mới nhất hiện có (`V30091802`). Các file gần đây có vẻ đặt tên theo `ddMMHHmm` (ví dụ `30091802` = ngày 30/09 lúc 18:02). Nếu tiếp tục kiểu đó cho ngày 10/11 thì `10111430` **nhỏ hơn** `30091802` về mặt số, Flyway sẽ coi là migration chen ngang (chưa bật `outOfOrder`) và báo lỗi khi khởi động. Vì vậy dùng `V31000001`.

`backend/src/main/resources/db/migration/V31000001__create_course_discounts_table.sql`

```sql
-- Mỗi khóa tối đa một khuyến mãi (UNIQUE course_id). Giảm theo phần trăm.
CREATE TABLE course_discounts (
    id               BIGINT    NOT NULL AUTO_INCREMENT,
    course_id        BIGINT    NOT NULL,
    discount_percent INT       NOT NULL,
    start_at         TIMESTAMP NOT NULL,
    end_at           TIMESTAMP NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_course_discount_course (course_id),
    CONSTRAINT fk_course_discount_course FOREIGN KEY (course_id) REFERENCES courses (id),
    CONSTRAINT chk_course_discount_percent CHECK (discount_percent BETWEEN 1 AND 90)
) ENGINE=InnoDB;
```

Dùng `INT` (không dùng `TINYINT`) để khớp với `Integer` của entity, tránh lỗi `ddl-auto: validate`.

### 4.2. Mã lỗi

Thêm vào `ErrorCode.java`, sau `CURRICULUM_ITEM_NOT_QUIZ(5013, ...)`:

```java
PRICE_INVALID(5014, "Giá khoá học không hợp lệ", HttpStatus.BAD_REQUEST),
DISCOUNT_INVALID(5015, "Thông tin khuyến mãi không hợp lệ", HttpStatus.BAD_REQUEST),
DISCOUNT_NOT_FOUND(5016, "Khoá học chưa có khuyến mãi", HttpStatus.NOT_FOUND),
```

### 4.3. Entity và repository

`feature/course/entity/CourseDiscount.java`

```java
package com.hnv.elearning.feature.course.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_discounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDiscount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false, updatable = false)
    private Long courseId;

    @Column(name = "discount_percent", nullable = false)
    private Integer discountPercent;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

`feature/course/repository/CourseDiscountRepository.java`

```java
public interface CourseDiscountRepository extends JpaRepository<CourseDiscount, Long> {
    Optional<CourseDiscount> findByCourseId(Long courseId);

    void deleteByCourseId(Long courseId);   // gọi trong @Transactional
}
```

Thêm vào `CourseRepository.java` (đã có `existsByIdAndInstructorId`):

```java
Optional<Course> findByIdAndInstructorId(Long id, Long instructorId);
```

### 4.4. DTO (`dto/privates/`)

```java
// CoursePricingUpdateRequest
@Getter @Setter
public class CoursePricingUpdateRequest {
    private BigDecimal price;       // 0 = miễn phí
}

// CourseDiscountRequest
@Getter @Setter
public class CourseDiscountRequest {
    private Integer percent;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}

// DiscountResponse
@Getter @Builder
public class DiscountResponse {
    private Integer percent;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String state;               // SCHEDULED | ACTIVE | EXPIRED
    private BigDecimal discountedPrice; // giá sau giảm
}

// CoursePricingResponse
@Getter @Builder
public class CoursePricingResponse {
    private BigDecimal price;           // null = chưa đặt giá
    private BigDecimal minPaidPrice;
    private BigDecimal maxPrice;
    private DiscountResponse discount;  // null = chưa có khuyến mãi
}
```

Các DTO response dùng `Integer`/`BigDecimal` (không dùng kiểu nguyên thủy `boolean`) để tránh lỗi tên thuộc tính của Jackson như `isCorrect` trước đây.

### 4.5. Service

`feature/course/service/CoursePricingService.java`

```java
public interface CoursePricingService {
    CoursePricingResponse getPricing(Long courseId, Long instructorId);
    void updatePrice(Long courseId, Long instructorId, CoursePricingUpdateRequest request);
    void saveDiscount(Long courseId, Long instructorId, CourseDiscountRequest request);
    void deleteDiscount(Long courseId, Long instructorId);
}
```

`feature/course/service/impl/CoursePricingServiceImpl.java`

```java
@Service
@RequiredArgsConstructor
public class CoursePricingServiceImpl implements CoursePricingService {
    private static final BigDecimal MIN_PAID_PRICE = BigDecimal.valueOf(50_000);
    private static final BigDecimal MAX_PRICE = BigDecimal.valueOf(50_000_000); // cột DECIMAL(10,2) chứa được ~99,9 triệu
    private static final int MAX_DISCOUNT_PERCENT = 90;

    private final CourseRepository courseRepository;
    private final CourseDiscountRepository discountRepository;

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Override
    public CoursePricingResponse getPricing(Long courseId, Long instructorId) {
        Course course = findOwnedCourse(courseId, instructorId);
        CourseDiscount discount = discountRepository.findByCourseId(courseId).orElse(null);
        return CoursePricingResponse.builder()
                .price(course.getPrice())
                .minPaidPrice(MIN_PAID_PRICE)
                .maxPrice(MAX_PRICE)
                .discount(discount == null ? null : toDiscountResponse(course.getPrice(), discount))
                .build();
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Override
    @Transactional
    public void updatePrice(Long courseId, Long instructorId, CoursePricingUpdateRequest request) {
        Course course = findOwnedCourse(courseId, instructorId);
        BigDecimal price = request.getPrice();
        validatePrice(price);

        course.setPrice(price);   // dirty checking, không cần save()
        if (price.signum() == 0) {
            discountRepository.deleteByCourseId(courseId);   // khóa miễn phí thì không có khuyến mãi
        }
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Override
    @Transactional
    public void saveDiscount(Long courseId, Long instructorId, CourseDiscountRequest request) {
        Course course = findOwnedCourse(courseId, instructorId);
        if (course.getPrice() == null || course.getPrice().signum() == 0) {
            throw new AppException(ErrorCode.DISCOUNT_INVALID);   // miễn phí / chưa đặt giá
        }
        validateDiscount(request);

        CourseDiscount discount = discountRepository.findByCourseId(courseId)
                .orElseGet(() -> CourseDiscount.builder().courseId(courseId).build());
        discount.setDiscountPercent(request.getPercent());
        discount.setStartAt(request.getStartAt());
        discount.setEndAt(request.getEndAt());
        discountRepository.save(discount);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Override
    @Transactional
    public void deleteDiscount(Long courseId, Long instructorId) {
        findOwnedCourse(courseId, instructorId);
        CourseDiscount discount = discountRepository.findByCourseId(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.DISCOUNT_NOT_FOUND));
        discountRepository.delete(discount);
    }

    private Course findOwnedCourse(Long courseId, Long instructorId) {
        return courseRepository.findByIdAndInstructorId(courseId, instructorId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_ACCESS_DENIED));
    }

    // 0 = miễn phí; có phí thì trong khoảng [MIN_PAID_PRICE, MAX_PRICE], là số nguyên (VND không có số lẻ)
    private void validatePrice(BigDecimal price) {
        if (price == null || price.signum() < 0
                || price.remainder(BigDecimal.ONE).signum() != 0
                || price.compareTo(MAX_PRICE) > 0
                || (price.signum() > 0 && price.compareTo(MIN_PAID_PRICE) < 0)) {
            throw new AppException(ErrorCode.PRICE_INVALID);
        }
    }

    private void validateDiscount(CourseDiscountRequest request) {
        Integer percent = request.getPercent();
        LocalDateTime start = request.getStartAt();
        LocalDateTime end = request.getEndAt();
        if (percent == null || percent < 1 || percent > MAX_DISCOUNT_PERCENT
                || start == null || end == null
                || !end.isAfter(start)
                || !end.isAfter(LocalDateTime.now())) {
            throw new AppException(ErrorCode.DISCOUNT_INVALID);
        }
    }

    private DiscountResponse toDiscountResponse(BigDecimal price, CourseDiscount discount) {
        LocalDateTime now = LocalDateTime.now();
        String state = now.isBefore(discount.getStartAt()) ? "SCHEDULED"
                : now.isAfter(discount.getEndAt()) ? "EXPIRED" : "ACTIVE";
        BigDecimal discounted = price == null ? null
                : price.multiply(BigDecimal.valueOf(100 - discount.getDiscountPercent()))
                       .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        return DiscountResponse.builder()
                .percent(discount.getDiscountPercent())
                .startAt(discount.getStartAt())
                .endAt(discount.getEndAt())
                .state(state)
                .discountedPrice(discounted)
                .build();
    }
}
```

`@Transactional` dùng đúng loại mà `CourseManagementServiceImpl` đang dùng (`jakarta.transaction.Transactional`) để thống nhất trong module.

### 4.6. Controller

`feature/course/controller/CoursePricingController.java`

```java
@RestController
@RequiredArgsConstructor
public class CoursePricingController {
    private final CoursePricingService pricingService;

    @GetMapping("/api/v1/courses/{courseId}/pricing")
    public ResponseEntity<ApiResponse<CoursePricingResponse>> getPricing(
            @PathVariable("courseId") Long courseId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(ApiResponse.<CoursePricingResponse>builder()
                .data(pricingService.getPricing(courseId, user.getId()))
                .code(HttpStatus.OK.value())
                .build());
    }

    @PutMapping("/api/v1/courses/{courseId}/pricing")
    public ResponseEntity<ApiResponse<Void>> updatePrice(
            @PathVariable("courseId") Long courseId,
            @AuthenticationPrincipal User user,
            @RequestBody CoursePricingUpdateRequest request
    ) {
        pricingService.updatePrice(courseId, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.<Void>builder().code(HttpStatus.OK.value()).build());
    }

    @PutMapping("/api/v1/courses/{courseId}/discount")
    public ResponseEntity<ApiResponse<Void>> saveDiscount(
            @PathVariable("courseId") Long courseId,
            @AuthenticationPrincipal User user,
            @RequestBody CourseDiscountRequest request
    ) {
        pricingService.saveDiscount(courseId, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.<Void>builder().code(HttpStatus.OK.value()).build());
    }

    @DeleteMapping("/api/v1/courses/{courseId}/discount")
    public ResponseEntity<ApiResponse<Void>> deleteDiscount(
            @PathVariable("courseId") Long courseId,
            @AuthenticationPrincipal User user
    ) {
        pricingService.deleteDiscount(courseId, user.getId());
        return ResponseEntity.ok(ApiResponse.<Void>builder().code(HttpStatus.OK.value()).build());
    }
}
```

Các API ghi trả `200` và không có body dữ liệu, FE gọi lại `GET /pricing` để lấy trạng thái mới nhất.

## 5. FE

### 5.1. `course.service.js`

```js
export const getCoursePricing = async (courseId) => {
    const { data } = await http.get(`/courses/${courseId}/pricing`);
    return data.data; // { price, minPaidPrice, maxPrice, discount: { percent, startAt, endAt, state, discountedPrice } | null }
};

export const updateCoursePrice = async (courseId, price) => {
    await http.put(`/courses/${courseId}/pricing`, { price });
};

export const saveCourseDiscount = async (courseId, payload) => {
    await http.put(`/courses/${courseId}/discount`, payload); // { percent, startAt, endAt }
};

export const deleteCourseDiscount = async (courseId) => {
    await http.delete(`/courses/${courseId}/discount`);
};
```

### 5.2. Route (`routes.jsx`)

```jsx
import CreateCoursePricing from "~pages/CreateCourse/CreateCoursePricing.jsx";
...
<Route path="/instructor/course/:courseId/version/:versionId/manage" element={<CreateCourseLayout/>}>
    <Route index element={<Navigate to="overview" replace />}></Route>
    <Route path="overview" element={<CreateCourseOverview/>}></Route>
    <Route path="curriculum" element={<CreateCourseCurriculum/>}></Route>
    <Route path="pricing" element={<CreateCoursePricing/>}></Route>   {/* thêm */}
</Route>
```

### 5.3. Sidebar

Trong `STEPS` của `CreateCourseSidebar.jsx`, đổi bước giá:

```js
{ key: "pricing", label: "Cài đặt giá & Khuyến mãi", icon: <TagOutlined />, ready: true },
```

### 5.4. Nút "Tiếp tục" ở trang Đề cương

Trong `CreateCourseCurriculum.jsx`: bỏ `handleSaveDraft`, state `saving` và icon `SaveOutlined` (không còn dùng), thêm:

```jsx
import { useNavigate, useOutletContext, useParams } from "react-router-dom";
import { ArrowRightOutlined } from "@ant-design/icons";   // thêm vào import icon hiện có
...
const navigate = useNavigate();
...
<Button
    type="primary"
    size="large"
    icon={<ArrowRightOutlined />}
    className={styles.submitBtn}
    onClick={() => navigate(`/instructor/course/${courseId}/version/${versionId}/manage/pricing`)}
>
    Tiếp tục
</Button>
```

Dùng đường dẫn tuyệt đối để khỏi nhầm cách React Router tính đường dẫn tương đối.

### 5.5. Trang `CreateCoursePricing.jsx`

`frontend/motedu/src/pages/CreateCourse/CreateCoursePricing.jsx`

```jsx
import { useCallback, useEffect, useState } from "react";
import { useOutletContext, useParams } from "react-router-dom";
import { Alert, Button, Card, DatePicker, InputNumber, Modal, Radio, Spin, Tag, message } from "antd";
import dayjs from "dayjs";
import { formatPrice } from "~/utils/course.util.js";
import {
    getCoursePricing, updateCoursePrice, saveCourseDiscount, deleteCourseDiscount,
} from "~services/course.service.js";
import styles from "./CreateCoursePricing.module.scss";

const DATE_FORMAT = "YYYY-MM-DDTHH:mm:ss"; // LocalDateTime phía BE, không kèm múi giờ

const STATE_TAG = {
    SCHEDULED: { color: "blue", text: "Sắp diễn ra" },
    ACTIVE: { color: "green", text: "Đang chạy" },
    EXPIRED: { color: "default", text: "Đã kết thúc" },
};

const thousands = (v) => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ".");

export default function CreateCoursePricing() {
    const { setPageTitle } = useOutletContext() || {};
    const { courseId } = useParams();

    const [pricing, setPricing] = useState(null);
    const [loading, setLoading] = useState(false);

    // Thẻ giá
    const [mode, setMode] = useState("paid"); // "free" | "paid"
    const [price, setPrice] = useState(null);
    const [savingPrice, setSavingPrice] = useState(false);

    // Thẻ khuyến mãi
    const [percent, setPercent] = useState(null);
    const [range, setRange] = useState(null); // [dayjs, dayjs]
    const [savingDiscount, setSavingDiscount] = useState(false);

    useEffect(() => {
        setPageTitle?.("Cài đặt giá & Khuyến mãi");
        return () => setPageTitle?.("");
    }, [setPageTitle]);

    const load = useCallback(async () => {
        setLoading(true);
        try {
            const data = await getCoursePricing(courseId);
            setPricing(data);
            // price null = chưa đặt giá: để chế độ "Có phí", ô nhập trống
            setMode(data.price === 0 ? "free" : "paid");
            setPrice(data.price > 0 ? data.price : null);
            setPercent(data.discount?.percent ?? null);
            setRange(data.discount ? [dayjs(data.discount.startAt), dayjs(data.discount.endAt)] : null);
        } catch (err) {
            message.error(err?.response?.data?.message ?? "Không thể tải thông tin giá");
        } finally {
            setLoading(false);
        }
    }, [courseId]);

    useEffect(() => {
        if (courseId) load();
    }, [courseId, load]);

    const handleSavePrice = async () => {
        const value = mode === "free" ? 0 : price;
        if (value === null || value === undefined) {
            message.warning("Vui lòng nhập giá bán");
            return;
        }
        setSavingPrice(true);
        try {
            await updateCoursePrice(courseId, value);
            message.success("Đã lưu giá");
            await load();
        } catch (err) {
            message.error(err?.response?.data?.message ?? "Không thể lưu giá");
        } finally {
            setSavingPrice(false);
        }
    };

    const handleSaveDiscount = async () => {
        if (!percent || !range) {
            message.warning("Vui lòng nhập phần trăm giảm và thời gian áp dụng");
            return;
        }
        setSavingDiscount(true);
        try {
            await saveCourseDiscount(courseId, {
                percent,
                startAt: range[0].format(DATE_FORMAT),
                endAt: range[1].format(DATE_FORMAT),
            });
            message.success("Đã lưu khuyến mãi");
            await load();
        } catch (err) {
            message.error(err?.response?.data?.message ?? "Không thể lưu khuyến mãi");
        } finally {
            setSavingDiscount(false);
        }
    };

    const handleDeleteDiscount = () =>
        Modal.confirm({
            title: "Xóa khuyến mãi này?",
            okText: "Xóa",
            cancelText: "Hủy",
            okButtonProps: { danger: true },
            onOk: async () => {
                try {
                    await deleteCourseDiscount(courseId);
                    message.success("Đã xóa khuyến mãi");
                    await load();
                } catch (err) {
                    message.error(err?.response?.data?.message ?? "Không thể xóa khuyến mãi");
                }
            },
        });

    const hasPaidPrice = pricing?.price > 0; // khuyến mãi chỉ áp dụng sau khi đã LƯU giá có phí
    const discount = pricing?.discount;
    const previewPrice = hasPaidPrice && percent
        ? Math.round((pricing.price * (100 - percent)) / 100)
        : null;

    return (
        <div className={styles.container}>
            <div className={styles.wrapper}>
                <h1 className={styles.pageTitle}>Bước 3: Cài đặt giá &amp; Khuyến mãi</h1>

                <Spin spinning={loading}>
                    <Card title="Giá khóa học" className={styles.card}>
                        <Radio.Group value={mode} onChange={(e) => setMode(e.target.value)}>
                            <Radio value="free">Miễn phí</Radio>
                            <Radio value="paid">Có phí</Radio>
                        </Radio.Group>

                        {mode === "paid" && (
                            <div className={styles.row}>
                                <InputNumber
                                    className={styles.priceInput}
                                    min={0}
                                    max={pricing?.maxPrice}
                                    step={10000}
                                    precision={0}
                                    value={price}
                                    onChange={setPrice}
                                    formatter={thousands}
                                    parser={(v) => v?.replace(/\./g, "")}
                                    suffix="₫"
                                    placeholder="Nhập giá bán"
                                />
                                {pricing && (
                                    <span className={styles.hint}>
                                        Từ {formatPrice(pricing.minPaidPrice)} đến {formatPrice(pricing.maxPrice)}
                                    </span>
                                )}
                            </div>
                        )}

                        <div className={styles.actions}>
                            <Button type="primary" loading={savingPrice} onClick={handleSavePrice}>
                                Lưu giá
                            </Button>
                        </div>
                    </Card>

                    <Card
                        title="Khuyến mãi"
                        className={styles.card}
                        extra={discount && (
                            <Tag color={STATE_TAG[discount.state]?.color}>{STATE_TAG[discount.state]?.text}</Tag>
                        )}
                    >
                        {!hasPaidPrice && (
                            <Alert
                                type="info"
                                showIcon
                                className={styles.alert}
                                title="Chỉ khóa học có phí mới có khuyến mãi. Hãy lưu giá có phí trước."
                            />
                        )}

                        <div className={styles.row}>
                            <span>Giảm</span>
                            <InputNumber
                                min={1}
                                max={90}
                                precision={0}
                                value={percent}
                                onChange={setPercent}
                                suffix="%"
                                disabled={!hasPaidPrice}
                            />
                        </div>
                        <div className={styles.row}>
                            <span>Thời gian</span>
                            <DatePicker.RangePicker
                                showTime
                                format="DD/MM/YYYY HH:mm"
                                value={range}
                                onChange={setRange}
                                disabled={!hasPaidPrice}
                                disabledDate={(d) => d && d.isBefore(dayjs(), "day")}
                            />
                        </div>
                        {previewPrice !== null && (
                            <p className={styles.preview}>
                                Giá sau giảm: <strong>{formatPrice(previewPrice)}</strong>
                            </p>
                        )}

                        <div className={styles.actions}>
                            {discount && (
                                <Button danger onClick={handleDeleteDiscount}>Xóa khuyến mãi</Button>
                            )}
                            <Button
                                type="primary"
                                loading={savingDiscount}
                                disabled={!hasPaidPrice}
                                onClick={handleSaveDiscount}
                            >
                                Lưu khuyến mãi
                            </Button>
                        </div>
                    </Card>
                </Spin>
            </div>
        </div>
    );
}
```

Ghi chú:
- Ký hiệu `₫` và `%` đặt bằng `suffix` của `InputNumber` (hiện ngay trong ô nhập).
- `Alert` của antd 6 dùng `title` (prop `message` đã bị đánh dấu deprecated, đã kiểm tra trong `node_modules/antd`).
- `dayjs` có sẵn trong `node_modules` (antd kéo theo) nhưng **không khai báo trong `package.json`**. Nên chạy `npm i dayjs` để khai báo rõ, tránh bị mất khi antd đổi phụ thuộc.
- Khuyến mãi dùng giá **đã lưu** (`pricing.price`), không dùng ô đang gõ. Đổi giá xong phải bấm "Lưu giá" thì phần khuyến mãi mới tính theo giá mới.

### 5.6. `CreateCoursePricing.module.scss`

```scss
.container {
  width: 100%;
  min-height: 100%;
  padding: 0 40px 60px;
  box-sizing: border-box;

  @media (max-width: 640px) {
    padding: 0 16px 40px;
  }
}

.wrapper {
  max-width: 720px;
  width: 100%;
  margin: 0 auto;
}

.pageTitle {
  margin: 24px 0 16px;
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
}

.card {
  margin-bottom: 20px;
}

.row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 16px;
}

.priceInput {
  width: 220px;
}

.hint {
  font-size: 12.5px;
  color: #94a3b8;
}

.alert {
  margin-bottom: 4px;
}

.preview {
  margin: 14px 0 0;
  color: #475569;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 20px;
}
```

## 6. Cách test

1. Chạy backend: Flyway phải tạo bảng `course_discounts`, `ddl-auto: validate` không báo lỗi lệch cột.
2. Khóa mới: vào trang giá, ô giá trống. Thẻ khuyến mãi hiện thông báo và các ô bị khóa.
3. Chọn "Có phí", nhập `10.000` rồi lưu: báo lỗi giá không hợp lệ (nhỏ hơn mức tối thiểu). Nhập `499.000` rồi lưu: thành công, F5 vẫn còn.
4. Nhập khuyến mãi 20%, chọn khoảng thời gian bao trùm hiện tại, lưu: thẻ trạng thái "Đang chạy", giá sau giảm `399.200 ₫`.
5. Chọn khoảng thời gian trong tương lai: "Sắp diễn ra". Đã kết thúc trong quá khứ: BE từ chối (`end` phải sau thời điểm hiện tại).
6. Đổi sang "Miễn phí" rồi lưu: khuyến mãi biến mất, thẻ khuyến mãi bị khóa lại.
7. Gọi API bằng tài khoản giảng viên khác (`courseId` của người khác): trả `403` `COURSE_ACCESS_DENIED`.
8. Bấm giữa các bước trong sidebar và nút "Tiếp tục" ở trang Đề cương: chuyển đúng sang trang giá, URL giữ nguyên `courseId` và `versionId`.

## 7. Chưa làm và cần lưu ý

- **Chưa nối giá sau giảm vào phía học viên.** Hiện các API public (`PublicCourseServiceImpl` `toCardDto`, `getCourseDetail`), lọc và sắp xếp theo giá (`CourseSpecification`, `course.price`), và lúc tạo đơn hàng đang chỉ dùng `courses.price`. Muốn học viên thấy và trả giá khuyến mãi thì phải tính giá hiệu lực (khuyến mãi đang `ACTIVE` thì dùng giá giảm), và `order_items.price_at_purchase` phải ghi đúng giá đó.
- Trạng thái khuyến mãi tính theo `LocalDateTime.now()` của server, không có múi giờ. FE gửi giờ địa phương của giảng viên, nên server và giảng viên nên cùng múi giờ.
- Chỉ một khuyến mãi mỗi khóa, và chỉ giảm theo phần trăm. Cần nhiều đợt hoặc giảm theo số tiền thì phải đổi bảng (bỏ `UNIQUE (course_id)`, thêm cột loại giảm).
- Giới hạn giá (50.000 đến 50.000.000) và mức giảm tối đa (90%) là hằng số tạm trong `CoursePricingServiceImpl`, cần chốt lại theo nghiệp vụ.
- Bước "Gửi duyệt" sau này phải kiểm tra `courses.price` khác `null`.
- Bước "Xem trước & Cài đặt" vẫn chưa có trang.
