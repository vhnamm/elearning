package com.hnv.elearning.feature.course.dto.instructor;

import com.hnv.elearning.feature.course.enums.CourseVersionStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorCourseItemDto {
    private Long id;
    private Long versionId;
    private Integer versionNo;
    private String title;
    private String thumbnailUrl;
    private BigDecimal price;
    private CourseVersionStatus status;
    private Integer totalEnrollments;
    private Double rating;
}
