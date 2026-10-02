package com.hnv.elearning.feature.category.service;

import com.hnv.elearning.feature.category.dto.CategoryDto;
import com.hnv.elearning.feature.category.dto.CategoryTreeDto;
import com.hnv.elearning.feature.category.dto.TopicDto;

import java.util.List;

public interface CategoryService {
    public List<CategoryDto> getAllCategory() ;
    public List<CategoryTreeDto> getCategoryTree();


}
