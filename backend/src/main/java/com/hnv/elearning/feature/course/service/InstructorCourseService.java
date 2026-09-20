package com.hnv.elearning.feature.course.service;

import com.hnv.elearning.feature.course.dto.CourseFilterRequest;
import com.hnv.elearning.feature.course.dto.InstructorCourseItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InstructorCourseService {
    Page<InstructorCourseItemDto> getMyCourses(CourseFilterRequest request, Long instructorId, Pageable pageable);
}
