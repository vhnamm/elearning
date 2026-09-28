package com.hnv.elearning.feature.course.controller;

import com.hnv.elearning.common.response.ApiResponse;
import com.hnv.elearning.feature.course.dto.CourseFilterRequest;
import com.hnv.elearning.feature.course.dto.PublicCourseCardDto;
import com.hnv.elearning.feature.course.service.PublicCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class PublicCourseController {
    private final PublicCourseService publicCourseService;

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<PublicCourseCardDto>>> getPopularCourses(
            @RequestParam(defaultValue = "4") int size
    ) {
        List<PublicCourseCardDto> data = publicCourseService.getPopularCourses(size);
        return ResponseEntity.ok(
                ApiResponse.<List<PublicCourseCardDto>>builder()
                        .code(HttpStatus.OK.value())
                        .data(data)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PublicCourseCardDto>>> getPublishedCourses(
            CourseFilterRequest request,
            @PageableDefault(page = 0, size = 8, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PublicCourseCardDto> data = publicCourseService.getPublishedCourses(request, pageable);
        return ResponseEntity.ok(
                ApiResponse.<Page<PublicCourseCardDto>>builder()
                        .code(HttpStatus.OK.value())
                        .data(data)
                        .build()
        );
    }
}
