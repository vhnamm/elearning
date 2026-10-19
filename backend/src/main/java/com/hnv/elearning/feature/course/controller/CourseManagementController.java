package com.hnv.elearning.feature.course.controller;

import com.hnv.elearning.common.response.ApiResponse;
import com.hnv.elearning.feature.course.dto.instructor.*;
import com.hnv.elearning.feature.course.dto.publics.CourseSearchRequest;
import com.hnv.elearning.feature.course.service.CourseManagementService;
import com.hnv.elearning.feature.user.entity.User;
import com.hnv.elearning.infrastructure.storage.PresignedUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CourseManagementController {
    private final CourseManagementService courseManagementService;

    @GetMapping("/api/v1/courses/instructed-courses")
    public ResponseEntity<?> getInstructorCourses(
            @AuthenticationPrincipal(expression = "id") Long instructorId,
            CourseSearchRequest request,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    )
    {
        var data = courseManagementService.getInstructedCourses(request, instructorId, pageable);
        ApiResponse<Page<InstructorCourseItemDto>> response = ApiResponse.<Page<InstructorCourseItemDto>>builder()
                .code(HttpStatus.OK.value())
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/api/v1/courses")
    public ResponseEntity<ApiResponse<CourseDraftResponse>> createDraftCourse(
            @RequestBody CourseDraftRequest courseDraftRequest,
            @AuthenticationPrincipal User user

    ){
        CourseDraftResponse dto = courseManagementService.createDraft(courseDraftRequest, user);
        ApiResponse apiResponse = ApiResponse
                .builder()
                .data(dto)
                .code(HttpStatus.CREATED.value())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/api/v1/courses/{courseId}/versions/{versionId}/basic-info")
    public ResponseEntity<ApiResponse<CourseBasicInfoResponse>> getCourseBasicInfo(
            @PathVariable(name = "courseId") Long courseId,
            @PathVariable(name = "versionId") Long versionId,
            @AuthenticationPrincipal User user
    ){
        CourseBasicInfoResponse dto = courseManagementService.getCourseBasicInfo(courseId, user.getId(), versionId);
        ApiResponse response =  ApiResponse.builder()
                .data(dto)
                .code(HttpStatus.OK.value())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }



    @PostMapping("/api/v1/courses/{id}/thumbnail/presigned-url")
    public ResponseEntity<ApiResponse<PresignedUploadResponse>> getPresignedUrl(
            @RequestBody CourseThumbnailPresignedRequest request,
            @PathVariable("id") Long courseId,
            @AuthenticationPrincipal User user
    ){
        PresignedUploadResponse rs = courseManagementService.getPresignedUrl(request, courseId, user);
        ApiResponse response = ApiResponse.builder()
                .code(HttpStatus.OK.value())
                .data(rs)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/api/v1/courses/{courseId}/versions/{versionId}/basic-info")
    public ResponseEntity<ApiResponse<Void>> updateCourseBasicInfo(
            @RequestBody CourseBasicInfoUpdateRequest request,
            @AuthenticationPrincipal User user,
            @PathVariable(name = "courseId") Long courseId,
            @PathVariable(name = "versionId") Long versionId
    ){
        courseManagementService.updateCourseBasicInfo(courseId, versionId, request, user);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/courses/{courseId}/versions/{versionId}/curriculum")
    public ResponseEntity<ApiResponse<CourseCurriculumInstructorResponse>> getCurriculum(
            @AuthenticationPrincipal User user,
            @PathVariable("versionId") Long versionId,
            @PathVariable("courseId") Long courseId
    ){
        CourseCurriculumInstructorResponse dto = courseManagementService.getCurriculumInstructor(user.getId(), courseId, versionId);
        ApiResponse<CourseCurriculumInstructorResponse> response = ApiResponse.<CourseCurriculumInstructorResponse>builder()
                .data(dto)
                .code(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(response);
    }
}
