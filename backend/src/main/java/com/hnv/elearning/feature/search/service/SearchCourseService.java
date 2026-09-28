package com.hnv.elearning.feature.search.service;

import com.hnv.elearning.feature.search.dto.CourseSearchResponse;
import com.hnv.elearning.feature.search.dto.SearchCourseRequest;

public interface SearchCourseService {
    CourseSearchResponse search(SearchCourseRequest request);
}
