package com.hnv.elearning.feature.search.dto;

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
    private List<SearchCourse> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
    private List<SearchFacet> categories;
    private List<SearchFacet> levels;
    private List<String> relatedQueries;
}
