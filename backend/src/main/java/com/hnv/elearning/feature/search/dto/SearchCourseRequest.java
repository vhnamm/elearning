package com.hnv.elearning.feature.search.dto;

import com.hnv.elearning.feature.course.enums.CourseLevel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class SearchCourseRequest {
    private String keyword;
    private List<Long> categoryIds;
    private List<Long> subcategoryIds;
    private List<Long> topicIds;
    private List<CourseLevel> levels;
    private Double minRating;
    private String priceType;
    private BigDecimal min;
    private BigDecimal max;
    private String sort;
    private Integer page;
    private Integer size;
}
