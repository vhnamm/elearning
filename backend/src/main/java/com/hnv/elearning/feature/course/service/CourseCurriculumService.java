package com.hnv.elearning.feature.course.service;

import com.hnv.elearning.feature.course.dto.instructor.CourseCurriculumInstructorResponse;
import com.hnv.elearning.feature.course.dto.publics.CourseCurriculumResponse;

public interface CourseCurriculumService {
    // Không kiểm tra quyền truy cập, service gọi tới phải tự kiểm tra trước.
    CourseCurriculumResponse getCurriculum(Long courseId);

    // Đề cương cho giảng viên soạn thảo. Không kiểm tra quyền: caller phải kiểm tra khóa thuộc giảng viên
    // và versionId thuộc đúng courseId trước khi gọi.
    CourseCurriculumInstructorResponse getInstructorCurriculum(Long versionId);
}
