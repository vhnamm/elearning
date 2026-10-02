package com.hnv.elearning.feature.category.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CategoryTreeDto {
    private Long id;
    private String name;
    private String slug;
    private List<SubcategoryDto> subcategories;
}
