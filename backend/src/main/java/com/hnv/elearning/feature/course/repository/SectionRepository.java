package com.hnv.elearning.feature.course.repository;

import com.hnv.elearning.feature.course.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {

    @Query("""
            SELECT DISTINCT s FROM Section s
            LEFT JOIN FETCH s.curriculumItems i
            LEFT JOIN FETCH i.lecture
            LEFT JOIN FETCH i.quizz
            WHERE s.course.id = :courseId
            ORDER BY s.position ASC
            """)
    List<Section> findCurriculumByCourseId(@Param("courseId") Long courseId);
}
