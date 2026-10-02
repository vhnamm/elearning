package com.hnv.elearning.feature.course.service.impl;

import com.hnv.elearning.feature.category.repository.CategoryRepository;
import com.hnv.elearning.feature.course.dto.CourseDetailDto;
import com.hnv.elearning.feature.course.dto.CourseSearchRequest;
import com.hnv.elearning.feature.course.dto.CourseSearchResponse;
import com.hnv.elearning.feature.course.dto.PublicCourseCardDto;
import com.hnv.elearning.feature.course.entity.Course;
import com.hnv.elearning.feature.course.mapper.CourseMapper;
import com.hnv.elearning.feature.course.repository.CourseRepository;
import com.hnv.elearning.feature.course.service.PublicCourseService;
import com.hnv.elearning.feature.course.specification.CourseSpecification;
import com.hnv.elearning.feature.enrollment.service.EnrollmentService;
import com.hnv.elearning.feature.review.service.CourseReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicCourseServiceImpl implements PublicCourseService {
    private static final Set<String> PRICE_TYPES = Set.of("ALL", "PAID", "FREE");
    private static final Set<String> SORTS = Set.of("popular", "rating", "newest", "price_asc", "price_desc");

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final EnrollmentService enrollmentService;
    private final CourseReviewService courseReviewService;
    private final CourseMapper courseMapper;

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

    // Chuẩn hóa tham số, lọc bằng CourseSpecification.buildSearchSpec và gắn kèm gợi ý từ khóa liên quan.
    @Override
    public CourseSearchResponse search(CourseSearchRequest request) {
        normalize(request);
        Specification<Course> spec = CourseSpecification.buildSearchSpec(request);
        Page<Course> result = courseRepository.findAll(spec, PageRequest.of(request.getPage(), request.getSize()));

        return CourseSearchResponse.builder()
                .content(toCardDtos(result.getContent()))
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .page(request.getPage())
                .size(request.getSize())
                .relatedQueries(relatedQueries(request.getKeyword()))
                .build();
    }

    // Gắn điểm đánh giá, số review và số học viên vào từng khóa trong trang hiện tại.
    private List<PublicCourseCardDto> toCardDtos(List<Course> courses) {
        if (courses.isEmpty()) {
            return List.of();
        }
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

    private PublicCourseCardDto toCardDto(Course course, Integer totalEnrollments, Double rating, Long reviewCount) {
        String instructorName = course.getInstructor() != null ? course.getInstructor().getFullName() : null;
        String subcategoryName = course.getSubcategory() != null ? course.getSubcategory().getName() : null;
        String categoryName = course.getCategory() != null ? course.getCategory().getName() : null;

        return PublicCourseCardDto.builder()
                .id(course.getId())
                .slug(course.getSlug())
                .title(course.getTitle())
                .shortDescription(course.getShortDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .price(course.getPrice())
                .level(course.getLevel() == null ? null : course.getLevel().name())
                .instructorName(instructorName)
                .categoryName(categoryName)
                .subcategoryName(subcategoryName)
                .rating(rating)
                .reviewCount(reviewCount)
                .totalEnrollments(totalEnrollments)
                .createdAt(course.getCreatedAt())
                .build();
    }

    // Lấy tối đa 6 gợi ý: danh mục/danh mục con khớp từ khóa, hoặc danh mục nhiều khóa nhất nếu chưa nhập từ khóa.
    private List<String> relatedQueries(String keyword) {
        if (keyword == null) {
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

    // Chuẩn hóa từ khóa, bộ lọc, sắp xếp và phân trang ngay trên request trước khi build specification.
    private void normalize(CourseSearchRequest request) {
        request.setKeyword(normalizeKeyword(request.getKeyword()));
        request.setCategoryIds(request.getCategoryIds() == null ? List.of() : request.getCategoryIds());
        request.setSubcategoryIds(request.getSubcategoryIds() == null ? List.of() : request.getSubcategoryIds());
        request.setLevels(request.getLevels() == null ? List.of() : request.getLevels());
        request.setMinRating(normalizeRating(request.getMinRating()));
        request.setPriceType(normalizePriceType(request.getPriceType()));

        BigDecimal min = normalizeMoney(request.getMin());
        BigDecimal max = normalizeMoney(request.getMax());
        if (min != null && max != null && min.compareTo(max) > 0) {
            BigDecimal swap = min;
            min = max;
            max = swap;
        }
        request.setMin(min);
        request.setMax(max);

        request.setSort(normalizeSort(request.getSort()));
        request.setPage(request.getPage() == null ? 0 : Math.max(request.getPage(), 0));
        request.setSize(request.getSize() == null ? 5 : Math.min(Math.max(request.getSize(), 1), 20));
    }

    // Bỏ khoảng trắng; từ khóa rỗng thì tìm toàn bộ khóa đã xuất bản.
    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    // Chỉ nhận điểm lọc trong khoảng lớn hơn 0 và không quá 5.
    private Double normalizeRating(Double minRating) {
        if (minRating == null || minRating <= 0 || minRating > 5) {
            return null;
        }
        return minRating;
    }

    // Chuẩn hóa loại giá về ALL, PAID hoặc FREE.
    private String normalizePriceType(String priceType) {
        if (priceType == null) {
            return "ALL";
        }
        String normalized = priceType.trim().toUpperCase(Locale.ROOT);
        return PRICE_TYPES.contains(normalized) ? normalized : "ALL";
    }

    // Bỏ giá âm; giá null nghĩa là không giới hạn đầu đó.
    private BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null || value.signum() < 0) {
            return null;
        }
        return value;
    }

    // Chỉ nhận các kiểu sắp xếp đã hỗ trợ, còn lại dùng phổ biến nhất.
    private String normalizeSort(String sort) {
        if (sort == null) {
            return "popular";
        }
        String normalized = sort.trim().toLowerCase(Locale.ROOT);
        return SORTS.contains(normalized) ? normalized : "popular";
    }

    @Override
    public CourseDetailDto getCourseDetail(Long courseId) {
        Course course = courseRepository.findCourseDetailById(courseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học với ID: " + courseId));

        CourseDetailDto dto = courseMapper.toCourseDetailDto(course);

        int totalDuration = 0;
        int totalQuizzes = 0;

        if (dto.getSections() != null) {
            for (CourseDetailDto.SectionDto section : dto.getSections()) {
                if (section.getCurriculumItems() != null) {
                    for (CourseDetailDto.CurriculumItemDto item : section.getCurriculumItems()) {
                        if ("LECTURE".equals(item.getType()) && item.getVideoDurationSeconds() != null) {
                            totalDuration += item.getVideoDurationSeconds();
                        } else if ("QUIZ".equals(item.getType())) {
                            totalQuizzes++;
                        }
                    }
                }
            }
        }
        dto.setTotalDurationSeconds(totalDuration);
        dto.setTotalQuizzes(totalQuizzes);

        List<Long> singleIdList = List.of(courseId);

        Map<Long, Integer> enrollmentMap = enrollmentService.getEnrolledCountByCourseIds(singleIdList);
        Map<Long, Double> ratingMap = courseReviewService.getAverageRatingsByCourseIds(singleIdList);
        Map<Long, Long> reviewCountMap = courseReviewService.getReviewCountsByCourseIds(singleIdList);

        dto.setStudentCount(enrollmentMap.getOrDefault(courseId, 0));
        dto.setAverageStar(ratingMap.getOrDefault(courseId, 0.0));
        dto.setReviewCount(reviewCountMap.getOrDefault(courseId, 0L).intValue());

        return dto;
    }

}
