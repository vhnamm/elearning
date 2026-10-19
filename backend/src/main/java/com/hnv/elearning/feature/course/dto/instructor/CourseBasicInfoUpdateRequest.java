package com.hnv.elearning.feature.course.dto.instructor;

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
public class CourseBasicInfoUpdateRequest {
    private String title;
    private String description;
    private String shortDescription;
    private String thumbnailKey;
    private String level;
    private List<String> learningOutcomes;
    private List<String> requiredSkills;
    private Long categoryId;
    private Long subcategoryId;
    private Set<Long> topicIds;
}
