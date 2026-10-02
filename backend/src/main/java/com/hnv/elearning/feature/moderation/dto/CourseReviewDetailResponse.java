package com.hnv.elearning.feature.moderation.dto;

import com.hnv.elearning.feature.course.enums.CourseLevel;
import com.hnv.elearning.feature.course.enums.CourseStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseReviewDetailResponse {
    private Long id;
    private String slug;
    private String title;
    private String shortDescription;
    private String description;
    private String thumbnailUrl;
    private BigDecimal price;
    private CourseLevel level;
    private CourseStatus status;
    private String categoryName;
    private String subcategoryName;
    private List<String> topics;
    private List<String> learningOutcomes;
    private List<String> requiredSkills;
    private InstructorInfo instructor;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InstructorInfo {
        private Long id;
        private String fullName;
        private String email;
        private String avatar;
    }
}
