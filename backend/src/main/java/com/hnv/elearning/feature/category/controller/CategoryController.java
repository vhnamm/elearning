package com.hnv.elearning.feature.category.controller;

import com.hnv.elearning.common.response.ApiResponse;
import com.hnv.elearning.feature.category.dto.CategoryDto;
import com.hnv.elearning.feature.category.repository.CategoryRepository;
import com.hnv.elearning.feature.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/api/v1/categories")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategory() {
        List<CategoryDto> result = categoryService.getAllCategory();

        ApiResponse response = ApiResponse.builder()
                .data(result)
                .build();

        return ResponseEntity.ok(response);

    }

}
