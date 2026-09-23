package com.hnv.elearning.feature.category.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CategoryDto {
    private Long id;
    private String name;
    private String slug;
}
