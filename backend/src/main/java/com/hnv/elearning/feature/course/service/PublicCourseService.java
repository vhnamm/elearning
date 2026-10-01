package com.hnv.elearning.feature.course.service;

import com.hnv.elearning.feature.course.dto.CourseDetailDto;
import com.hnv.elearning.feature.course.dto.CourseSearchRequest;
import com.hnv.elearning.feature.course.dto.CourseSearchResponse;
import com.hnv.elearning.feature.course.dto.PublicCourseCardDto;

import java.util.List;

public interface PublicCourseService {
    List<PublicCourseCardDto> getPopularCourses(int size);

    // Tìm khóa học đã xuất bản theo từ khóa, bộ lọc, sắp xếp và phân trang.
    CourseSearchResponse search(CourseSearchRequest request);

    CourseDetailDto getCourseDetail(Long courseId);
}
