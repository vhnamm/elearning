package com.hnv.elearning.feature.course.dto;

import com.hnv.elearning.feature.course.enums.CourseStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorCourseItemDto {
    private Long id;
    private String title;
    private String thumbnailUrl;
    private BigDecimal price;
    private CourseStatus status;
    private Integer totalEnrollments;
    private Double rating;
}
