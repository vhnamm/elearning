package com.hnv.elearning.feature.course.service;

import com.hnv.elearning.feature.course.dto.instructor.*;
import com.hnv.elearning.feature.course.dto.publics.CourseSearchRequest;
import com.hnv.elearning.feature.user.entity.User;
import com.hnv.elearning.infrastructure.storage.PresignedUploadResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseManagementService {
    Page<InstructorCourseItemDto> getInstructedCourses(CourseSearchRequest request, Long instructorId, Pageable pageable);
    CourseDraftResponse createDraft(CourseDraftRequest courseDraftRequest, User user);

    CourseBasicInfoResponse getCourseBasicInfo(Long courseId, Long instructorId, Long versionId);

    CourseCurriculumInstructorResponse getCurriculumInstructor(Long instructorId, Long courseId, Long versionId);

    PresignedUploadResponse getPresignedUrl(CourseThumbnailPresignedRequest request, Long courseId, User user);

    void updateCourseBasicInfo(Long courseId, Long versionId, CourseBasicInfoUpdateRequest request, User user);
}
