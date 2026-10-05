package com.hnv.elearning.feature.category.mapper;

import com.hnv.elearning.feature.category.dto.CategoryDto;
import com.hnv.elearning.feature.category.dto.CategoryTreeDto;
import com.hnv.elearning.feature.category.dto.SubcategoryDto;
import com.hnv.elearning.feature.category.entity.Category;
import com.hnv.elearning.feature.category.entity.Subcategory;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring", nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface CategoryMapper {
    CategoryDto categoryToCategoryDto(Category category);
    CategoryTreeDto categoryToCategoryTreeDto(Category category);
    SubcategoryDto subcategoryToSubcategoryDto(Subcategory subcategory);
}
