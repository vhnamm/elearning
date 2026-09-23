package com.hnv.elearning.feature.category.service;

import com.hnv.elearning.feature.category.dto.CategoryDto;
import com.hnv.elearning.feature.category.entity.Category;
import com.hnv.elearning.feature.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDto> getAllCategory() {
        //check, gọi service
        List<Category> result = categoryRepository.findAll();

        return result.stream().map(
                category -> new CategoryDto(category.getId(), category.getSlug(), category.getName())
        ).toList();

    }
}
