package com.hnv.elearning.feature.moderation.dto;

import com.hnv.elearning.feature.course.enums.CourseLevel;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingCourseItemDto {
    private Long id;
    private String title;
    private String thumbnailUrl;
    private String categoryName;
    private String subcategoryName;
    private BigDecimal price;
    private CourseLevel level;
    private Long instructorId;
    private String instructorName;
    private String instructorAvatar;
    private LocalDateTime submittedAt;
}
