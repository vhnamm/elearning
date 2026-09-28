package com.hnv.elearning.feature.search.service;

import com.hnv.elearning.feature.search.dto.CourseSearchResponse;
import com.hnv.elearning.feature.search.dto.SearchCourseRequest;

public interface SearchCourseService {
    // Tìm khóa học đã xuất bản theo từ khóa, bộ lọc, sắp xếp và phân trang.
    CourseSearchResponse search(SearchCourseRequest request);
}
