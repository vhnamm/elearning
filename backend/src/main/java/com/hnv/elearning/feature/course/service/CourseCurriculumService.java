package com.hnv.elearning.feature.course.service;

import com.hnv.elearning.feature.course.dto.CourseCurriculumResponse;

public interface CourseCurriculumService {
    // Không kiểm tra quyền truy cập, service gọi tới phải tự kiểm tra trước.
    CourseCurriculumResponse getCurriculum(Long courseId);
}
