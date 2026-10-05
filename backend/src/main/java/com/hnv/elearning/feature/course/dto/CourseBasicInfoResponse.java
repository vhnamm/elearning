package com.hnv.elearning.feature.course.dto;

import com.hnv.elearning.feature.category.dto.TopicDto;
import com.hnv.elearning.feature.course.entity.CourseRequiredSkill;
import com.hnv.elearning.feature.course.enums.CourseLevel;
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
    private Long categoryId;
    private Long subcategoryId;
    private String title;
    private CourseLevel level;
    private String description;
    private String shortDescription;
    private String thumbnailUrl;
    private Set<CourseRequiredSkillResponse> requiredSkills;
    private Set<CourseLearningOutcomeResponse> learningOutcomes;
    private Set<TopicDto> topics;
}
