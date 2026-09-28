package com.hnv.elearning.feature.course.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicCourseCardDto {
    private Long id;
    private String title;
    private String thumbnailUrl;
    private BigDecimal price;
    private String instructorName;
    private Double rating;
    private Long reviewCount;
    private Integer totalEnrollments;
}
