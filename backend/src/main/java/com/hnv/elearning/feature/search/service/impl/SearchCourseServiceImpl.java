package com.hnv.elearning.feature.search.service.impl;

import com.hnv.elearning.feature.category.entity.Category;
import com.hnv.elearning.feature.category.entity.Subcategory;
import com.hnv.elearning.feature.category.repository.CategoryRepository;
import com.hnv.elearning.feature.course.entity.Course;
import com.hnv.elearning.feature.course.enums.CourseLevel;
import com.hnv.elearning.feature.course.repository.CourseRepository;
import com.hnv.elearning.feature.course.specification.CourseSpecification;
import com.hnv.elearning.feature.enrollment.entity.Enrollment;
import com.hnv.elearning.feature.enrollment.enums.EnrollmentStatus;
import com.hnv.elearning.feature.enrollment.service.EnrollmentService;
import com.hnv.elearning.feature.review.entity.CourseReview;
import com.hnv.elearning.feature.review.service.CourseReviewService;
import com.hnv.elearning.feature.search.dto.CourseSearchResponse;
import com.hnv.elearning.feature.search.dto.SearchCourse;
import com.hnv.elearning.feature.search.dto.SearchCourseRequest;
import com.hnv.elearning.feature.search.dto.SearchFacet;
import com.hnv.elearning.feature.search.service.SearchCourseService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchCourseServiceImpl implements SearchCourseService {
    private static final Set<String> PRICE_TYPES = Set.of("ALL", "PAID", "FREE");
    private static final Set<String> SORTS = Set.of("popular", "rating", "newest", "price_asc", "price_desc");
    private static final List<CourseLevel> LEVEL_ORDER = List.of(
            CourseLevel.BEGINNER,
            CourseLevel.INTERMEDIATE,
            CourseLevel.ADVANCED
    );

    private final EntityManager entityManager;
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final EnrollmentService enrollmentService;
    private final CourseReviewService courseReviewService;

    // Chuẩn hóa tham số rồi trả danh sách khóa, số lượng lọc và gợi ý liên quan.
    @Override
    public CourseSearchResponse search(SearchCourseRequest request) {
        String keyword = normalizeKeyword(request.getKeyword());
        List<Long> categoryIds = request.getCategoryIds() == null ? List.of() : request.getCategoryIds();
        List<CourseLevel> levels = request.getLevels() == null ? List.of() : request.getLevels();
        Double minRating = normalizeRating(request.getMinRating());
        String priceType = normalizePriceType(request.getPriceType());
        BigDecimal min = normalizeMoney(request.getMin());
        BigDecimal max = normalizeMoney(request.getMax());
        if (min != null && max != null && min.compareTo(max) > 0) {
            BigDecimal swap = min;
            min = max;
            max = swap;
        }
        String sort = normalizeSort(request.getSort());
        int page = request.getPage() == null ? 0 : Math.max(request.getPage(), 0);
        int size = request.getSize() == null ? 5 : Math.min(Math.max(request.getSize(), 1), 20);

        Specification<Course> spec = CourseSpecification.forSearch(
                keyword, categoryIds, levels, minRating, priceType, min, max
        );
        List<Course> courses = findPage(spec, sort, page, size);
        long total = count(spec);

        List<SearchCourse> content = toCards(courses);
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);

        return CourseSearchResponse.builder()
                .content(content)
                .totalElements(total)
                .totalPages(totalPages)
                .page(page)
                .size(size)
                .categories(categoryFacets(keyword, levels, minRating, priceType, min, max))
                .levels(levelFacets(keyword, categoryIds, minRating, priceType, min, max))
                .relatedQueries(relatedQueries(keyword))
                .build();
    }

    // Gắn điểm đánh giá, số review và số học viên vào từng khóa trong trang hiện tại.
    private List<SearchCourse> toCards(List<Course> courses) {
        if (courses.isEmpty()) {
            return List.of();
        }
        List<Long> courseIds = courses.stream().map(Course::getId).toList();
        Map<Long, Integer> enrollmentCountMap = enrollmentService.getEnrolledCountByCourseIds(courseIds);
        Map<Long, Double> avgRatingMap = courseReviewService.getAverageRatingsByCourseIds(courseIds);
        Map<Long, Long> reviewCountMap = courseReviewService.getReviewCountsByCourseIds(courseIds);

        return courses.stream()
                .map(course -> toCard(
                        course,
                        enrollmentCountMap.getOrDefault(course.getId(), 0),
                        avgRatingMap.getOrDefault(course.getId(), 0.0),
                        reviewCountMap.getOrDefault(course.getId(), 0L)
                ))
                .toList();
    }

    // Đổi một Course entity thành dữ liệu thẻ hiển thị trên trang tìm kiếm.
    private SearchCourse toCard(Course course, Integer totalEnrollments, Double rating, Long reviewCount) {
        String instructorName = course.getInstructor() != null ? course.getInstructor().getFullName() : null;
        String subcategoryName = course.getSubcategory() != null ? course.getSubcategory().getName() : null;
        String categoryName = course.getSubcategory() != null && course.getSubcategory().getCategory() != null
                ? course.getSubcategory().getCategory().getName()
                : null;

        return SearchCourse.builder()
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
                .badge(resolveBadge(course, rating, reviewCount))
                .build();
    }

    // Chọn nhãn trên ảnh: miễn phí, đánh giá cao, hoặc mới trong 60 ngày.
    private String resolveBadge(Course course, Double rating, Long reviewCount) {
        if (course.getPrice() != null && course.getPrice().compareTo(BigDecimal.ZERO) == 0) {
            return "FREE";
        }
        if (rating != null && rating >= 4.5 && reviewCount != null && reviewCount > 0) {
            return "TOP_RATED";
        }
        if (course.getCreatedAt() != null && course.getCreatedAt().isAfter(LocalDateTime.now().minusDays(60))) {
            return "NEW";
        }
        return null;
    }

    // Đếm khóa đã xuất bản theo từng danh mục cha, không áp bộ lọc danh mục đang chọn.
    private List<SearchFacet> categoryFacets(
            String keyword,
            List<CourseLevel> levels,
            Double minRating,
            String priceType,
            BigDecimal min,
            BigDecimal max
    ) {
        Specification<Course> spec = CourseSpecification.forSearch(
                keyword, List.of(), levels, minRating, priceType, min, max
        );
        Map<Long, Long> counts = countByCategory(spec);
        return categoryRepository.findAll().stream()
                .sorted((left, right) -> left.getName().compareToIgnoreCase(right.getName()))
                .map(category -> SearchFacet.builder()
                        .id(category.getId())
                        .key(String.valueOf(category.getId()))
                        .name(category.getName())
                        .count(counts.getOrDefault(category.getId(), 0L))
                        .build())
                .toList();
    }

    // Đếm khóa đã xuất bản theo từng cấp độ, không áp bộ lọc cấp độ đang chọn.
    private List<SearchFacet> levelFacets(
            String keyword,
            List<Long> categoryIds,
            Double minRating,
            String priceType,
            BigDecimal min,
            BigDecimal max
    ) {
        Specification<Course> spec = CourseSpecification.forSearch(
                keyword, categoryIds, List.of(), minRating, priceType, min, max
        );
        Map<String, Long> counts = countByLevel(spec);
        return LEVEL_ORDER.stream()
                .map(level -> SearchFacet.builder()
                        .key(level.name())
                        .name(level.name())
                        .count(counts.getOrDefault(level.name(), 0L))
                        .build())
                .toList();
    }

    // Nhóm số khóa học theo id danh mục cha.
    private Map<Long, Long> countByCategory(Specification<Course> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Course> root = query.from(Course.class);
        Join<Course, Subcategory> subcategory = root.join("subcategory", JoinType.INNER);
        Join<Subcategory, Category> category = subcategory.join("category", JoinType.INNER);
        Predicate predicate = spec.toPredicate(root, query, cb);
        query.multiselect(category.get("id"), cb.countDistinct(root.get("id")));
        if (predicate != null) {
            query.where(predicate);
        }
        query.groupBy(category.get("id"));

        Map<Long, Long> counts = new LinkedHashMap<>();
        for (Tuple tuple : entityManager.createQuery(query).getResultList()) {
            counts.put(tuple.get(0, Long.class), tuple.get(1, Long.class));
        }
        return counts;
    }

    // Nhóm số khóa học theo cấp độ BEGINNER, INTERMEDIATE, ADVANCED.
    private Map<String, Long> countByLevel(Specification<Course> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Course> root = query.from(Course.class);
        Predicate predicate = spec.toPredicate(root, query, cb);
        query.multiselect(root.get("level"), cb.countDistinct(root.get("id")));
        if (predicate != null) {
            query.where(predicate);
        }
        query.groupBy(root.get("level"));

        Map<String, Long> counts = new LinkedHashMap<>();
        for (Tuple tuple : entityManager.createQuery(query).getResultList()) {
            CourseLevel level = tuple.get(0, CourseLevel.class);
            if (level == null) {
                continue;
            }
            counts.put(level.name(), tuple.get(1, Long.class));
        }
        return counts;
    }

    // Lấy tối đa 6 gợi ý: danh mục khớp từ khóa, hoặc danh mục nhiều khóa nhất nếu chưa nhập từ khóa.
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

    // Lấy một trang khóa học đã lọc và sắp xếp.
    private List<Course> findPage(Specification<Course> spec, String sort, int page, int size) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Course> query = cb.createQuery(Course.class);
        Root<Course> root = query.from(Course.class);
        Predicate predicate = spec.toPredicate(root, query, cb);
        if (predicate != null) {
            query.where(predicate);
        }
        query.orderBy(buildOrders(sort, root, query, cb));
        return entityManager.createQuery(query)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    // Đếm tổng khóa học khớp bộ lọc, dùng cho phân trang.
    private long count(Specification<Course> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Course> root = query.from(Course.class);
        Predicate predicate = spec.toPredicate(root, query, cb);
        query.select(cb.countDistinct(root.get("id")));
        if (predicate != null) {
            query.where(predicate);
        }
        return entityManager.createQuery(query).getSingleResult();
    }

    // Dựng thứ tự sắp xếp: phổ biến, đánh giá, mới nhất, hoặc giá.
    private List<Order> buildOrders(String sort, Root<Course> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Order> orders = new ArrayList<>();
        switch (sort) {
            case "rating" -> orders.add(cb.desc(averageRating(root, query, cb)));
            case "newest" -> orders.add(cb.desc(root.get("createdAt")));
            case "price_asc" -> orders.add(cb.asc(root.get("price")));
            case "price_desc" -> orders.add(cb.desc(root.get("price")));
            default -> {
                orders.add(cb.desc(enrollmentCount(root, query, cb)));
                orders.add(cb.desc(averageRating(root, query, cb)));
            }
        }
        if (!"newest".equals(sort)) {
            orders.add(cb.desc(root.get("createdAt")));
        }
        orders.add(cb.desc(root.get("id")));
        return orders;
    }

    // Điểm trung bình review của khóa, khóa chưa có review được tính là 0.
    private Expression<Double> averageRating(Root<Course> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Subquery<Double> subquery = query.subquery(Double.class);
        Root<CourseReview> review = subquery.from(CourseReview.class);
        subquery.select(cb.avg(review.get("rating")));
        subquery.where(cb.equal(review.get("course"), root));
        return cb.coalesce(subquery, 0.0);
    }

    // Số lượt ghi danh còn hiệu lực, không tính bản ghi đã thu hồi.
    private Expression<Long> enrollmentCount(Root<Course> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Enrollment> enrollment = subquery.from(Enrollment.class);
        subquery.select(cb.count(enrollment));
        subquery.where(
                cb.equal(enrollment.get("course"), root),
                cb.notEqual(enrollment.get("status"), EnrollmentStatus.REVOKED)
        );
        return cb.coalesce(subquery, 0L);
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
}
