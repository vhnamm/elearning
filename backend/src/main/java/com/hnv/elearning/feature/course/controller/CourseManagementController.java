package com.hnv.elearning.feature.course.controller;

import com.hnv.elearning.common.response.ApiResponse;
import com.hnv.elearning.feature.course.dto.*;
import com.hnv.elearning.feature.course.service.CourseManagementService;
import com.hnv.elearning.feature.user.entity.User;
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
        var data = courseManagementService.getMyCourses(request, instructorId, pageable);
        ApiResponse<Page<InstructorCourseItemDto>> response = ApiResponse.<Page<InstructorCourseItemDto>>builder()
                .code(HttpStatus.OK.value())
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/api/v1/courses")
    public ResponseEntity<ApiResponse<CourseDraftDto>> createDraftCourse(
            @RequestBody CourseDraftRequest courseDraftRequest,
            @AuthenticationPrincipal User user

    ){
        CourseDraftDto dto = courseManagementService.createDraft(courseDraftRequest, user);
        ApiResponse apiResponse = ApiResponse
                .builder()
                .data(dto)
                .code(HttpStatus.CREATED.value())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/api/v1/courses/{id}/basic-info")
    public ResponseEntity<ApiResponse<CourseBasicInfoResponse>> getCourseBasicInfo(
            @PathVariable(name = "id") Long courseId,
            @AuthenticationPrincipal User user
    ){
        CourseBasicInfoResponse dto = courseManagementService.getCourseBasicInfo(courseId, user.getId());
        ApiResponse response =  ApiResponse.builder()
                .data(dto)
                .code(HttpStatus.OK.value())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/api/v1/instructor/courses/{id}/curriculum")
    public ResponseEntity<ApiResponse<CourseCurriculumResponse>> getCourseCurriculum(
            @PathVariable(name = "id") Long courseId,
            @AuthenticationPrincipal User user
    ) {
        CourseCurriculumResponse dto = courseManagementService.getCourseCurriculum(courseId, user.getId());
        return ResponseEntity.ok(
                ApiResponse.<CourseCurriculumResponse>builder()
                        .code(HttpStatus.OK.value())
                        .data(dto)
                        .build()
        );
    }
}
