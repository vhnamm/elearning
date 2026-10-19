package com.hnv.elearning.feature.course.specification;

import com.hnv.elearning.feature.course.entity.CourseVersion;
import com.hnv.elearning.feature.course.enums.CourseVersionStatus;
import org.springframework.data.jpa.domain.Specification;

// Spec cho màn "Khóa của tôi" của giảng viên: danh sách là các version (mỗi version một card), gốc query là CourseVersion.
public class CourseVersionSpecification {

    // Version thuộc khóa của giảng viên này.
    public static Specification<CourseVersion> belongsToInstructor(Long instructorId) {
        return (root, query, cb) ->
                instructorId == null ? null : cb.equal(root.get("course").get("instructor").get("id"), instructorId);
    }

    // Bản RETIRED là lịch sử, không hiển thị trong danh sách.
    public static Specification<CourseVersion> isNotRetired() {
        return (root, query, cb) -> cb.notEqual(root.get("status"), CourseVersionStatus.RETIRED);
    }

    // Lọc theo tab trạng thái; null nghĩa là "Tất cả".
    public static Specification<CourseVersion> hasStatus(CourseVersionStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<CourseVersion> titleContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            return cb.like(cb.lower(root.get("title")), CourseSpecification.likePattern(keyword), '\\');
        };
    }

    public static Specification<CourseVersion> forInstructor(Long instructorId, CourseVersionStatus status, String keyword) {
        return Specification.where(belongsToInstructor(instructorId))
                .and(isNotRetired())
                .and(hasStatus(status))
                .and(titleContains(keyword));
    }
}
