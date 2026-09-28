package com.hnv.elearning.feature.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCourse {
    private Long id;
    private String slug;
    private String title;
    private String shortDescription;
    private String thumbnailUrl;
    private BigDecimal price;
    private String level;
    private String instructorName;
    private String categoryName;
    private String subcategoryName;
    private Double rating;
    private Long reviewCount;
    private Integer totalEnrollments;
    private LocalDateTime createdAt;
    private String badge;
}
