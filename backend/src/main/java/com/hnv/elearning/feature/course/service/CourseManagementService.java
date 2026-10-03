package com.hnv.elearning.feature.course.service;

import com.hnv.elearning.feature.course.dto.*;
import com.hnv.elearning.feature.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseManagementService {
    Page<InstructorCourseItemDto> getMyCourses(CourseSearchRequest request, Long instructorId, Pageable pageable);
    CourseDraftDto createDraft(CourseDraftRequest courseDraftRequest, User user);

    CourseBasicInfoResponse getCourseBasicInfo(Long courseId, Long instructorId);

    CourseCurriculumResponse getCourseCurriculum(Long courseId, Long instructorId);
}
