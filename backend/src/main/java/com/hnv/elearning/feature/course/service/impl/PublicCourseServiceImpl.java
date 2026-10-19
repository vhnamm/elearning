package com.hnv.elearning.feature.course.service.impl;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import com.hnv.elearning.feature.category.repository.CategoryRepository;
import com.hnv.elearning.feature.course.dto.publics.*;
import com.hnv.elearning.feature.course.entity.Course;
import com.hnv.elearning.feature.course.entity.CourseVersion;
import com.hnv.elearning.feature.course.enums.CourseStatus;
import com.hnv.elearning.feature.course.mapper.CourseMapper;
import com.hnv.elearning.feature.course.repository.CourseRepository;
import com.hnv.elearning.feature.course.repository.CourseVersionRepository;
import com.hnv.elearning.feature.course.service.CourseCurriculumService;
import com.hnv.elearning.feature.course.service.PublicCourseService;
import com.hnv.elearning.feature.course.specification.CourseSpecification;
import com.hnv.elearning.feature.enrollment.service.EnrollmentService;
import com.hnv.elearning.feature.review.service.CourseReviewService;
import com.hnv.elearning.feature.user.service.UserService;
import com.hnv.elearning.infrastructure.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
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
    private final CourseVersionRepository courseVersionRepository;
    private final StorageService storageService;;

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
        String subcategoryName = course.getPublishedVersion().getSubcategory() != null ? course.getPublishedVersion().getSubcategory().getName() : null;

        CourseVersion version = course.getPublishedVersion();

        return PublicCourseCardDto.builder()
                .id(course.getId())
                .slug(course.getSlug())
                .title(version.getTitle())
                .shortDescription(version.getShortDescription())
                .thumbnailUrl(version.getThumbnailKey() == null ? null : storageService.publicUrl(version.getThumbnailKey()))
                .price(course.getPrice())
                .level(version.getLevel() == null ? null : version.getLevel().name())
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
    public CourseDetailResponse getCourseDetail(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        // Công khai chỉ đọc bản đang published; khóa chưa có bản published coi như không tồn tại.
        if (course.getStatus() != CourseStatus.PUBLISHED || course.getPublishedVersion() == null) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }

        CourseVersion version = courseVersionRepository
                .findPublicDetailByIdAndCourseId(course.getPublishedVersion().getId(), courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        CourseDetailResponse dto = courseMapper.toCourseDetailDto(version);
        dto.setThumbnailUrl(version.getThumbnailKey() == null ? null : storageService.publicUrl(version.getThumbnailKey()));
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
        //tim version dang dc Published
        Long publishedVersionId = courseRepository.getPublishedVersionId(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        return courseCurriculumService.getCurriculum(publishedVersionId);
    }

}
