package com.hnv.elearning.feature.course.service;

import com.hnv.elearning.feature.course.dto.CourseFilterRequest;
import com.hnv.elearning.feature.course.dto.PublicCourseCardDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PublicCourseService {
    List<PublicCourseCardDto> getPopularCourses(int size);

    Page<PublicCourseCardDto> getPublishedCourses(CourseFilterRequest request, Pageable pageable);
}
