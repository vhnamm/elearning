package com.hnv.elearning.feature.category.service;

import com.hnv.elearning.feature.category.dto.CategoryDto;
import com.hnv.elearning.feature.category.dto.CategoryTreeDto;
import com.hnv.elearning.feature.category.entity.Category;
import com.hnv.elearning.feature.category.mapper.CategoryMapper;
import com.hnv.elearning.feature.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryDto> getAllCategory() {
        //check, gọi service
        List<Category> result = categoryRepository.findAll();

        return result.stream().map(
                category -> categoryMapper.categoryToCategoryDto(category)
        ).toList();

    }

    @Override
    public List<CategoryTreeDto> getCategoryTree() {
        List<Category> result = categoryRepository.findAllWithSubcategories();

        return result.stream().map(
                category -> categoryMapper.categoryToCategoryTreeDto(category)
        ).toList();

    }
}
