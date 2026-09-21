package com.hnv.elearning.feature.enrollment.repository;

import com.hnv.elearning.feature.course.entity.Course;
import com.hnv.elearning.feature.enrollment.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment,Long> {

    @Query("SELECT c.id, COUNT(e) " +
            "FROM Course c " +
            "LEFT JOIN Enrollment e ON e.course = c " +
            "AND e.status = 'ACTIVE' " +
            "WHERE c.id IN :courseIds " +
            "GROUP BY c.id")
    List<Object[]> countEnrolledByCourseIds(@Param("courseIds") Collection<Long> courseIds);

    Collection<Long> course(Course course);
}
