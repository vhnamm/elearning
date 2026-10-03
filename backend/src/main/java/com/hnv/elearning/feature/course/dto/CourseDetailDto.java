package com.hnv.elearning.feature.course.dto;

import com.hnv.elearning.feature.course.enums.CourseStatus;
import com.hnv.elearning.feature.user.dto.InstructorInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailDto {
    private Long id;
    private String title;
    private String thumbnailUrl;
    private String shortDescription;
    private String description;
    private Double price;
    private String language;
    private LocalDateTime updatedAt;

    private Double averageStar;
    private Integer reviewCount;
    private Integer studentCount;

    @Builder.Default
    private boolean hasCertificate = true;

    private List<CourseLearningOutcomeResponse> learningOutcomes;

    private CourseStatus status;

    private List<CourseRequiredSkillResponse> requiredSkills;

    private InstructorInfoDto instructor;
}