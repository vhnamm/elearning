package com.hnv.elearning.feature.course.dto;

import com.hnv.elearning.feature.course.entity.CourseRequiredSkill;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseBasicInfoResponse {
    private Long id;
    private String title;
    private String description;
    private String shortDescription;
    private String thumbnailUrl;
    private Set<CourseRequiredSkillResponse> requiredSkills;
    private Set<CourseLearningOutcomeResponse> learningOutcomes;
}
