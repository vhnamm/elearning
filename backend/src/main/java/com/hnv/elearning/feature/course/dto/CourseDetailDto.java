package com.hnv.elearning.feature.course.dto;

import com.hnv.elearning.feature.course.enums.CourseStatus;
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
    private Double price;
    private String language;
    private LocalDateTime updatedAt;

    private Double averageStar;
    private Integer reviewCount;
    private Integer studentCount;

    private Integer totalDurationSeconds;
    private Integer totalQuizzes;

    @Builder.Default
    private boolean hasCertificate = true;

    private List<CourseLearningOutcomeResponse> learningOutcomes;

    private List<SectionDto> sections;

    private CourseStatus status;

    private List<CourseRequiredSkillResponse> requiredSkills;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectionDto {
        private Long id;
        private String title;
        private int position;
        private List<CurriculumItemDto> curriculumItems;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurriculumItemDto {
        private Long id;
        private String title;
        private String type;
        private int position;
        private boolean isPreview;
        private String videoKey;
        private Integer videoDurationSeconds;
        private Integer passingScore;
    }

    private InstructorInfoDto instructor;
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstructorInfoDto {
        private Long id;
        private String fullName;
        private String avatar;
    }
}