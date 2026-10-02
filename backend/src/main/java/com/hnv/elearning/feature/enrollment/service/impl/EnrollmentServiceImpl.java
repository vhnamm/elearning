package com.hnv.elearning.feature.enrollment.service.impl;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import com.hnv.elearning.feature.course.entity.Course;
import com.hnv.elearning.feature.course.repository.CourseRepository;
import com.hnv.elearning.feature.enrollment.entity.Enrollment;
import com.hnv.elearning.feature.enrollment.enums.EnrollmentStatus;
import com.hnv.elearning.feature.enrollment.repository.EnrollmentRepository;
import com.hnv.elearning.feature.enrollment.service.EnrollmentService;
import com.hnv.elearning.feature.user.entity.User;
import com.hnv.elearning.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Override
    public Map<Long, Integer> getEnrolledCountByCourseIds(Collection<Long> courseIds) {
        List<Object[]> list = enrollmentRepository.countEnrolledByCourseIds(courseIds);

        return list.stream().collect(Collectors.toMap(
                item -> (Long) item[0],
                item -> ((Number) item[1]).intValue()
        ));
    }

    @Override
    public boolean checkUserEnrolled(Long courseId, Long userId) {
        return enrollmentRepository.existsByCourse_IdAndStudent_Id(courseId, userId);
    }

    @Override
    @Transactional
    public void enrollFreeCourse(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại"));

        if (course.getPrice() != null && course.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Khóa học này có tính phí.");
        }

        if (enrollmentRepository.existsByCourse_IdAndStudent_Id(courseId, userId)) {
            throw new RuntimeException("Bạn đã sở hữu khóa học này rồi!");
        }

        User student = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học viên"));

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.save(enrollment);
    }
}