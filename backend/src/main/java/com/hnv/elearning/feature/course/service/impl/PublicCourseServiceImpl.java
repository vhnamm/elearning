package com.hnv.elearning.feature.course.service.impl;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import com.hnv.elearning.feature.category.repository.CategoryRepository;
import com.hnv.elearning.feature.course.dto.CourseCurriculumResponse;
import com.hnv.elearning.feature.course.dto.CourseDetailDto;
import com.hnv.elearning.feature.course.dto.CourseSearchRequest;
import com.hnv.elearning.feature.course.dto.CourseSearchResponse;
import com.hnv.elearning.feature.course.dto.PublicCourseCardDto;
import com.hnv.elearning.feature.course.entity.Course;
import com.hnv.elearning.feature.course.enums.CourseStatus;
import com.hnv.elearning.feature.course.mapper.CourseMapper;
import com.hnv.elearning.feature.course.repository.CourseRepository;
import com.hnv.elearning.feature.course.service.CourseCurriculumService;
import com.hnv.elearning.feature.course.service.PublicCourseService;
import com.hnv.elearning.feature.course.specification.CourseSpecification;
import com.hnv.elearning.feature.enrollment.service.EnrollmentService;
import com.hnv.elearning.feature.review.service.CourseReviewService;
import com.hnv.elearning.feature.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PublicCourseServiceImpl implements PublicCourseService {
    private static final int POPULAR_WINDOW_DAYS = 30;

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final EnrollmentService enrollmentService;
    private final UserService userService;
    private final CourseCurriculumService courseCurriculumService;
    private final CourseReviewService courseReviewService;
    private final CourseMapper courseMapper;

    @Override
    public List<PublicCourseCardDto> getPopularCourses(int size) {
        int limit = Math.max(1, Math.min(size, 20));
        LocalDateTime since = LocalDateTime.now().minusDays(POPULAR_WINDOW_DAYS);
        return toCardDtos(courseRepository.findPopularCourses(since, PageRequest.of(0, limit)));
    }

    // Lọc bằng CourseSpecification.buildSearchSpec rồi phân trang.
    @Override
    public CourseSearchResponse search(CourseSearchRequest request) {
        Specification<Course> spec = CourseSpecification.buildSearchSpec(request);
        Page<Course> result = courseRepository.findAll(spec, PageRequest.of(request.getPage(), request.getSize()));

        return CourseSearchResponse.builder()
                .content(toCardDtos(result.getContent()))
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .page(request.getPage())
                .size(request.getSize())
                .build();
    }

    // Gắn điểm đánh giá và số review vào từng khóa trong trang hiện tại.
    private List<PublicCourseCardDto> toCardDtos(List<Course> courses) {
        if (courses.isEmpty()) {
            return List.of();
        }
        List<Long> courseIds = courses.stream().map(Course::getId).toList();
        Map<Long, CourseReviewService.RatingSummary> summaries = courseReviewService.getRatingSummariesByCourseIds(courseIds);

        return courses.stream()
                .map(course -> {
                    CourseReviewService.RatingSummary s = summaries.getOrDefault(course.getId(), CourseReviewService.RatingSummary.EMPTY);
                    return toCardDto(course, s.averageRating(), s.reviewCount());
                })
                .toList();
    }

    private PublicCourseCardDto toCardDto(Course course, Double rating, Long reviewCount) {
        String instructorName = course.getInstructor() != null ? course.getInstructor().getFullName() : null;
        String subcategoryName = course.getSubcategory() != null ? course.getSubcategory().getName() : null;

        return PublicCourseCardDto.builder()
                .id(course.getId())
                .slug(course.getSlug())
                .title(course.getTitle())
                .shortDescription(course.getShortDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .price(course.getPrice())
                .level(course.getLevel() == null ? null : course.getLevel().name())
                .instructorName(instructorName)
                .subcategoryName(subcategoryName)
                .rating(rating)
                .reviewCount(reviewCount)
                .createdAt(course.getCreatedAt())
                .build();
    }

    // Lấy tối đa 6 gợi ý: danh mục/danh mục con khớp từ khóa, hoặc danh mục nhiều khóa nhất nếu chưa nhập từ khóa.
    @Override
    public List<String> getRelatedQueries(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return courseRepository.findPopularCategoryNames(PageRequest.of(0, 6));
        }
        String pattern = CourseSpecification.likePattern(keyword);
        LinkedHashMap<String, String> related = new LinkedHashMap<>();
        addRelated(related, courseRepository.findRelatedSubcategoryNames(pattern, PageRequest.of(0, 8)), keyword);
        addRelated(related, courseRepository.findRelatedCategoryNames(pattern, PageRequest.of(0, 8)), keyword);
        if (related.isEmpty()) {
            addRelated(related, categoryRepository.findNamesByKeyword(pattern, PageRequest.of(0, 6)), keyword);
        }
        return related.values().stream().limit(6).toList();
    }

    // Thêm tên gợi ý, bỏ trùng và bỏ đúng từ khóa đang tìm.
    private void addRelated(Map<String, String> target, Collection<String> names, String keyword) {
        for (String name : names) {
            if (name == null || name.isBlank() || name.equalsIgnoreCase(keyword)) {
                continue;
            }
            target.putIfAbsent(name.toLowerCase(Locale.ROOT), name);
        }
    }

    @Override
    public CourseDetailDto getCourseDetail(Long courseId) {
        Course course = courseRepository.findCourseDetailById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));


        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }

        CourseDetailDto dto = courseMapper.toCourseDetailDto(course);
        if (course.getInstructor() != null) {
            dto.setInstructor(userService.getInstructorInfo(course.getInstructor().getId()));
        }

        List<Long> singleIdList = List.of(courseId);

        Map<Long, Integer> enrollmentMap = enrollmentService.getEnrolledCountByCourseIds(singleIdList);
        CourseReviewService.RatingSummary summary = courseReviewService.getRatingSummariesByCourseIds(singleIdList)
                .getOrDefault(courseId, CourseReviewService.RatingSummary.EMPTY);

        dto.setStudentCount(enrollmentMap.getOrDefault(courseId, 0));
        dto.setAverageStar(summary.averageRating());
        dto.setReviewCount((int) summary.reviewCount());

        return dto;
    }

    @Override
    public CourseCurriculumResponse getCourseCurriculum(Long courseId) {
        if (!courseRepository.existsByIdAndStatus(courseId, CourseStatus.PUBLISHED)) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        return courseCurriculumService.getCurriculum(courseId);
    }

}
