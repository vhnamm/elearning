package com.hnv.elearning.feature.course.service.impl;

import com.hnv.elearning.feature.course.dto.CourseFilterRequest;
import com.hnv.elearning.feature.course.dto.PublicCourseCardDto;
import com.hnv.elearning.feature.course.entity.Course;
import com.hnv.elearning.feature.course.repository.CourseRepository;
import com.hnv.elearning.feature.course.service.PublicCourseService;
import com.hnv.elearning.feature.course.specification.CourseSpecification;
import com.hnv.elearning.feature.enrollment.service.EnrollmentService;
import com.hnv.elearning.feature.review.service.CourseReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicCourseServiceImpl implements PublicCourseService {
    private final CourseRepository courseRepository;
    private final EnrollmentService enrollmentService;
    private final CourseReviewService courseReviewService;

    @Override
    public List<PublicCourseCardDto> getPopularCourses(int size) {
        int limit = Math.max(1, Math.min(size, 20));
        Specification<Course> spec = CourseSpecification.forPublic(null, null, null);
        List<Course> courses = courseRepository.findAll(spec);

        if (courses.isEmpty()) {
            return List.of();
        }

        List<PublicCourseCardDto> cards = toCardDtos(courses);
        return cards.stream()
                .sorted(Comparator
                        .comparing((PublicCourseCardDto c) -> c.getTotalEnrollments() == null ? 0 : c.getTotalEnrollments())
                        .reversed()
                        .thenComparing(c -> c.getRating() == null ? 0.0 : c.getRating(), Comparator.reverseOrder()))
                .limit(limit)
                .toList();
    }

    @Override
    public Page<PublicCourseCardDto> getPublishedCourses(CourseFilterRequest request, Pageable pageable) {
        Specification<Course> spec = CourseSpecification.forPublic(
                request.getKeyword(),
                request.getMin(),
                request.getMax()
        );
        Page<Course> courses = courseRepository.findAll(spec, pageable);

        if (courses.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> courseIds = courses.stream().map(Course::getId).toList();
        Map<Long, Integer> enrollmentCountMap = enrollmentService.getEnrolledCountByCourseIds(courseIds);
        Map<Long, Double> avgRatingMap = courseReviewService.getAverageRatingsByCourseIds(courseIds);
        Map<Long, Long> reviewCountMap = courseReviewService.getReviewCountsByCourseIds(courseIds);

        return courses.map(course -> toCardDto(
                course,
                enrollmentCountMap.getOrDefault(course.getId(), 0),
                avgRatingMap.getOrDefault(course.getId(), 0.0),
                reviewCountMap.getOrDefault(course.getId(), 0L)
        ));
    }

    private List<PublicCourseCardDto> toCardDtos(List<Course> courses) {
        List<Long> courseIds = courses.stream().map(Course::getId).toList();
        Map<Long, Integer> enrollmentCountMap = enrollmentService.getEnrolledCountByCourseIds(courseIds);
        Map<Long, Double> avgRatingMap = courseReviewService.getAverageRatingsByCourseIds(courseIds);
        Map<Long, Long> reviewCountMap = courseReviewService.getReviewCountsByCourseIds(courseIds);

        return courses.stream()
                .map(course -> toCardDto(
                        course,
                        enrollmentCountMap.getOrDefault(course.getId(), 0),
                        avgRatingMap.getOrDefault(course.getId(), 0.0),
                        reviewCountMap.getOrDefault(course.getId(), 0L)
                ))
                .toList();
    }

    private PublicCourseCardDto toCardDto(
            Course course,
            Integer totalEnrollments,
            Double rating,
            Long reviewCount
    ) {
        String instructorName = course.getInstructor() != null
                ? course.getInstructor().getFullName()
                : null;

        return PublicCourseCardDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .thumbnailUrl(course.getThumbnailUrl())
                .price(course.getPrice())
                .instructorName(instructorName)
                .rating(rating)
                .reviewCount(reviewCount)
                .totalEnrollments(totalEnrollments)
                .build();
    }
}
