package com.hnv.elearning.feature.category.controller;

import com.hnv.elearning.common.response.ApiResponse;
import com.hnv.elearning.feature.category.dto.TopicDto;
import com.hnv.elearning.feature.category.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TopicController {
    private final TopicService topicService;

    @GetMapping("/api/v1/topics")
    public ResponseEntity<ApiResponse<List<TopicDto>>> searchTopics(
            @RequestParam(name = "keyword", required = false) String keyword
    ) {
        List<TopicDto> rs = topicService.searchTopics(keyword);

        ApiResponse response = ApiResponse
                .builder()
                .data(rs)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/topics/trending")
    public ResponseEntity<ApiResponse<List<TopicDto>>> trendingTopics(Pageable pageable) {
        List<TopicDto> rs = topicService.getTrendingTopics();

        ApiResponse apiResponse = ApiResponse.builder()
                .data(rs)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

}
