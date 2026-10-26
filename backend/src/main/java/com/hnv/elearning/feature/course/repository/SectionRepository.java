package com.hnv.elearning.feature.course.repository;

import com.hnv.elearning.feature.course.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Long> {

    @Query("""
            SELECT DISTINCT s FROM Section s
            LEFT JOIN FETCH s.curriculumItems i
            LEFT JOIN FETCH i.lecture
            LEFT JOIN FETCH i.quizz
            WHERE s.courseVersion.id = :versionId
            ORDER BY s.position ASC, i.position ASC
            """)
    List<Section> findCurriculumByVersionId(@Param("versionId") Long versionId);

    Optional<Section> findByIdAndCourseVersionId(Long sectionId, Long versionId);

    @Query("""
            SELECT s FROM Section s
            JOIN FETCH s.courseVersion v
            WHERE s.id = :sectionId AND v.course.instructor.id = :instructorId
            """)
    Optional<Section> findOwnedSection(@Param("sectionId") Long sectionId, @Param("instructorId") Long instructorId);

    @Query("SELECT COALESCE(MAX(s.position), 0)  FROM Section s WHERE s.courseVersion.id = :versionId")
    int findMaxPosition(@Param("versionId") Long versionId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(""" 
            UPDATE Section s SET s.position = s.position - 1 
            WHERE s.courseVersion.id = :versionId AND s.position > :position
           """)
    void shiftPositionAfterDelete(@Param("versionId") Long versionId, @Param("position") int position);


    List<Section> findSectionByCourseVersionId(@Param("versionId") Long versionId);
}
