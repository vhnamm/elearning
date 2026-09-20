package com.hnv.elearning.feature.course.dto;

import com.hnv.elearning.feature.course.enums.CourseStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CourseFilterRequest {
    private String keyword;
    private BigDecimal min;
    private BigDecimal max;
    private CourseStatus status;
}
