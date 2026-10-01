package com.hnv.elearning.feature.course.controller;

import com.hnv.elearning.common.response.ApiResponse;
import com.hnv.elearning.feature.course.dto.CourseDetailDto;
import com.hnv.elearning.feature.course.dto.CourseSearchRequest;
import com.hnv.elearning.feature.course.dto.CourseSearchResponse;
import com.hnv.elearning.feature.course.dto.PublicCourseCardDto;
import com.hnv.elearning.feature.course.service.PublicCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // API công khai cho trang tìm kiếm / khám phá khóa học: GET /api/v1/courses.
    @GetMapping
    public ResponseEntity<ApiResponse<CourseSearchResponse>> search(CourseSearchRequest request) {
        CourseSearchResponse data = publicCourseService.search(request);
        return ResponseEntity.ok(
                ApiResponse.<CourseSearchResponse>builder()
                        .code(HttpStatus.OK.value())
                        .data(data)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDetailDto>> getCourseDetail(@PathVariable("id") Long id) {
        CourseDetailDto result = publicCourseService.getCourseDetail(id);

        return ResponseEntity.ok(
                ApiResponse.<CourseDetailDto>builder()
                        .code(HttpStatus.OK.value())
                        .data(result)
                        .build()
        );
    }
}
