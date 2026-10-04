package com.hnv.elearning.feature.course.dto;

import com.hnv.elearning.feature.course.enums.CourseLevel;
import com.hnv.elearning.feature.course.enums.CourseStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CourseSearchRequest {
    private String keyword;
    private List<Long> categoryIds;
    private List<Long> subcategoryIds;
    private Long topicId;
    private List<CourseLevel> levels;
    private Double minRating;
    private String priceType;
    private BigDecimal min;
    private BigDecimal max;
    private CourseStatus status;
    private String sort;
    private Integer page = 0;
    private Integer size = 5;
}
