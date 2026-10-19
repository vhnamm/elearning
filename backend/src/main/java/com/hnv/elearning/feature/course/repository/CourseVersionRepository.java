package com.hnv.elearning.feature.course.repository;

import com.hnv.elearning.feature.course.entity.CourseVersion;
import com.hnv.elearning.feature.course.enums.CourseVersionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface CourseVersionRepository extends JpaRepository<CourseVersion, Long>, JpaSpecificationExecutor<CourseVersion> {

    // Danh sách version cho màn "Khóa của tôi"; fetch kèm course để card đọc price/id không bị N+1.
    @Override
    @EntityGraph(attributePaths = {"course"})
    Page<CourseVersion> findAll(Specification<CourseVersion> spec, Pageable pageable);

    // Bản đang mở (DRAFT/PENDING_REVIEW/REJECTED) của khóa; tối đa 1 nhờ open_course_id.
    Optional<CourseVersion> findFirstByCourseIdAndStatusIn(Long courseId, Collection<CourseVersionStatus> statuses);



    @EntityGraph(attributePaths = {"learningOutcomes", "requiredSkills", "category", "subcategory", "topics", "course"})
    Optional<CourseVersion> findWithDetailByIdAndCourseId(Long versionId, Long courseId);

    @EntityGraph(attributePaths = {"learningOutcomes", "requiredSkills", "course"})
    Optional<CourseVersion> findPublicDetailByIdAndCourseId(Long versionId, Long courseId);


    @Query("""
        SELECT COALESCE(MAX(v.versionNo), 0) FROM CourseVersion v WHERE v.course.id = :courseId
    """)
    int findMaxVersionNo(@Param("courseId") Long courseId);


    boolean existsByIdAndCourseId(Long id, Long courseId);

    Optional<CourseVersion> findByIdAndCourseId(Long id, Long courseId);
}
