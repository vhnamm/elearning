package com.hnv.elearning.feature.course.dto;

import com.hnv.elearning.feature.course.enums.CourseLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CourseBasicInfoRequest {
    private String title;
    private String description;
    private String shortDescription;
    private Long categoryId;
    private Long subcategoryId;
    private List<String> requiredSkills;
    private List<String> learningOutcomes;
    private CourseLevel level;
    private Set<Long> topicIds;

}
