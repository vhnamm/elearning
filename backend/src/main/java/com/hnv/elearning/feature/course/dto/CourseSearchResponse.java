package com.hnv.elearning.feature.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSearchResponse {
    private List<PublicCourseCardDto> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
    private List<String> relatedQueries;
}
