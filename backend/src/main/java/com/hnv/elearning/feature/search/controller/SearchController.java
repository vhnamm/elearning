package com.hnv.elearning.feature.search.controller;

import com.hnv.elearning.common.response.ApiResponse;
import com.hnv.elearning.feature.search.dto.CourseSearchResponse;
import com.hnv.elearning.feature.search.dto.SearchCourseRequest;
import com.hnv.elearning.feature.search.service.SearchCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchCourseService searchCourseService;

    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<CourseSearchResponse>> search(SearchCourseRequest request) {
        CourseSearchResponse data = searchCourseService.search(request);
        return ResponseEntity.ok(
                ApiResponse.<CourseSearchResponse>builder()
                        .code(HttpStatus.OK.value())
                        .data(data)
                        .build()
        );
    }
}
