package com.hnv.elearning.feature.course.service.impl;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import com.hnv.elearning.common.utils.StringUtil;
import com.hnv.elearning.feature.category.entity.Category;
import com.hnv.elearning.feature.category.repository.CategoryRepository;
import com.hnv.elearning.feature.course.dto.*;
import com.hnv.elearning.feature.course.entity.Course;
import com.hnv.elearning.feature.course.enums.CourseStatus;
import com.hnv.elearning.feature.course.mapper.CourseMapper;
import com.hnv.elearning.feature.course.repository.CourseRepository;
import com.hnv.elearning.feature.course.service.CourseManagementService;
import com.hnv.elearning.feature.course.specification.CourseSpecification;
import com.hnv.elearning.feature.enrollment.service.EnrollmentService;
import com.hnv.elearning.feature.review.service.CourseReviewService;
import com.hnv.elearning.feature.user.entity.User;
import com.hnv.elearning.feature.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseManagementManagementServiceImpl implements CourseManagementService {
    private final CourseRepository courseRepository;
    private final EnrollmentService enrollmentService;
    private final CourseReviewService  courseReviewService;
    private final UserRepository userRepository;
    private final CourseMapper courseMapper;
    private final CategoryRepository categoryRepository;

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Override
    public Page<InstructorCourseItemDto> getMyCourses(CourseSearchRequest request, Long instructorId, Pageable pageable) {
        Specification spec = CourseSpecification.forInstructor(instructorId, request.getStatus(), request.getKeyword());
        Page<Course> courses = courseRepository.findAll(spec, pageable);

        if(courses.isEmpty()){
            return Page.empty(pageable);
        }

        List<Long> courseIds = courses.stream().map(
                course -> course.getId()
        ).toList();

        Map<Long, Integer> enrollmentCountMap = enrollmentService.getEnrolledCountByCourseIds(courseIds);
        Map<Long, Double> avgRatingMap = courseReviewService.getAverageRatingsByCourseIds(courseIds);

        return courses.map(
                course -> {
                    return InstructorCourseItemDto.builder()
                            .id(course.getId())
                            .rating(avgRatingMap.get(course.getId()))
                            .price(course.getPrice())
                            .status(course.getStatus())
                            .thumbnailUrl(course.getThumbnailUrl())
                            .title(course.getTitle())
                            .totalEnrollments(enrollmentCountMap.get(course.getId()))
                            .build();
                }
        );

    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Transactional
    @Override
    public CourseDraftDto createDraft(CourseDraftRequest courseDraftRequest, User user){
        User instructor = userRepository.findById(user.getId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Category category = categoryRepository.findById(courseDraftRequest.getCategoryId()).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        Course course = courseMapper.toEntity(courseDraftRequest);
        course.setInstructor(instructor);
        course.setCategory(category);
        course.setStatus(CourseStatus.DRAFT);
        course.setSlug(StringUtil.toSlug(courseDraftRequest.getTitle()));
        Course savedCourse = courseRepository.save(course);
        return courseMapper.toDto(savedCourse);

    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Override
    public CourseBasicInfoResponse getCourseBasicInfo(Long courseId, Long instructorId) {
        if (!courseRepository.existsByIdAndInstructorId(courseId, instructorId)) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        Course course = courseRepository.findWithDetailById(courseId).orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        CourseBasicInfoResponse response = courseMapper.toBasicInfoResponse(course);

        return response;
    }
}
