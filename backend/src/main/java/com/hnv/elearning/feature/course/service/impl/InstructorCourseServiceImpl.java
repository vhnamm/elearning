package com.hnv.elearning.feature.course.service.impl;

import com.hnv.elearning.feature.course.dto.CourseFilterRequest;
import com.hnv.elearning.feature.course.dto.InstructorCourseItemDto;
import com.hnv.elearning.feature.course.entity.Course;
import com.hnv.elearning.feature.course.repository.CourseRepository;
import com.hnv.elearning.feature.course.service.InstructorCourseService;
import com.hnv.elearning.feature.course.specification.CourseSpecification;
import com.hnv.elearning.feature.enrollment.service.EnrollmentService;
import com.hnv.elearning.feature.review.service.CourseReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InstructorCourseServiceImpl implements InstructorCourseService {
    private final CourseRepository courseRepository;
    private final EnrollmentService enrollmentService;
    private final CourseReviewService  courseReviewService;


    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Override
    public Page<InstructorCourseItemDto> getMyCourses(CourseFilterRequest request, Long instructorId, Pageable pageable) {
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
}
