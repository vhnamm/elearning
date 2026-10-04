package com.hnv.elearning.feature.course.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicCourseCardDto {
    private Long id;
    private String slug;
    private String title;
    private String shortDescription;
    private String thumbnailUrl;
    private BigDecimal price;
    private String level;
    private String instructorName;
    private String subcategoryName;
    private Double rating;
    private Long reviewCount;
    private LocalDateTime createdAt;
}
