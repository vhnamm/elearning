package com.hnv.elearning.feature.course.repository;

import com.hnv.elearning.feature.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    @Override
    Page<Course> findAll(Specification<Course> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"learningOutcomes", "requiredSkills"})
    Optional<Course> findWithDetailById(Long id);

    boolean existsByIdAndInstructorId(Long id, Long instructorId);
}
