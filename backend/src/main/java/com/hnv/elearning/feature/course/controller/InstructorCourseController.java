package com.hnv.elearning.feature.course.controller;

import com.hnv.elearning.common.response.ApiResponse;
import com.hnv.elearning.feature.course.dto.CourseFilterRequest;
import com.hnv.elearning.feature.course.dto.InstructorCourseItemDto;
import com.hnv.elearning.feature.course.service.InstructorCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/instructor")
@RequiredArgsConstructor
public class InstructorCourseController {
    private final InstructorCourseService instructorCourseService;

    @GetMapping("/courses")
    public ResponseEntity<?> getInstructorCourses(
            @AuthenticationPrincipal(expression = "id") Long instructorId,
            CourseFilterRequest courseFilterRequest,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    )
    {
        var data = instructorCourseService.getMyCourses(courseFilterRequest, instructorId, pageable);
        ApiResponse<Page<InstructorCourseItemDto>> response = ApiResponse.<Page<InstructorCourseItemDto>>builder()
                .code(HttpStatus.OK.value())
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
