package com.hnv.elearning.feature.moderation.dto;

import com.hnv.elearning.feature.course.enums.CourseStatus;
import com.hnv.elearning.feature.moderation.enums.ModerationAction;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModerationResultResponse {
    private Long courseId;
    private CourseStatus status;
    private ModerationAction action;
    private String feedback;
    private LocalDateTime moderatedAt;
}
