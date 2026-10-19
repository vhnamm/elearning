package com.hnv.elearning.feature.course.dto.instructor;

import com.hnv.elearning.feature.category.dto.TopicDto;
import com.hnv.elearning.feature.course.dto.publics.CourseLearningOutcomeResponse;
import com.hnv.elearning.feature.course.dto.publics.CourseRequiredSkillResponse;
import com.hnv.elearning.feature.course.enums.CourseLevel;
import com.hnv.elearning.feature.course.enums.CourseVersionStatus;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseBasicInfoResponse {
    private Long id;
    private Long versionId;
    private Integer versionNo;
    private CourseVersionStatus status;
    private Long categoryId;
    private Long subcategoryId;
    private String title;
    private CourseLevel level;
    private String description;
    private String shortDescription;
    private String thumbnailKey;
    private String thumbnailUrl;
    private Set<CourseRequiredSkillResponse> requiredSkills;
    private Set<CourseLearningOutcomeResponse> learningOutcomes;
    private Set<TopicDto> topics;
}
