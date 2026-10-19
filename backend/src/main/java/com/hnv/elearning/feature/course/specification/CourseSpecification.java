package com.hnv.elearning.feature.course.specification;

import com.hnv.elearning.feature.course.dto.publics.CourseSearchRequest;
import com.hnv.elearning.feature.course.entity.Course;
import com.hnv.elearning.feature.course.entity.CourseVersion;
import com.hnv.elearning.feature.course.enums.CourseLevel;
import com.hnv.elearning.feature.course.enums.CourseStatus;
import com.hnv.elearning.feature.enrollment.entity.Enrollment;
import com.hnv.elearning.feature.enrollment.enums.EnrollmentStatus;
import com.hnv.elearning.feature.review.entity.CourseReview;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class CourseSpecification {
    // Chỉ khóa đã có bản published mới hiển thị công khai. Chỉ kiểm tra cột published_version_id, không sinh join.
    public static Specification<Course> hasPublishedVersion() {
        return (root, query, cb) -> cb.isNotNull(root.get("publishedVersion"));
    }

    // Nội dung (title, category, topics...) nằm ở bản đang published; join một lần rồi dùng lại cho mọi spec.
    @SuppressWarnings("unchecked")
    private static Join<Course, CourseVersion> version(Root<Course> root) {
        return root.getJoins().stream()
                .filter(j -> j.getAttribute().getName().equals("publishedVersion"))
                .map(j -> (Join<Course, CourseVersion>) j)
                .findFirst()
                .orElseGet(() -> root.join("publishedVersion", JoinType.INNER));
    }

    public static Specification<Course> hasCourseStatus(CourseStatus status){
        return (root, query, cb) ->
                status == null ? null :
                cb.equal(root.get("status"), status);
    }

    // Khớp từ khóa với tên khóa, mô tả ngắn và giảng viên. Không còn khớp theo tên danh mục/danh mục con.
    public static Specification<Course> matchesKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            String pattern = likePattern(keyword);
            Expression<String> title = cb.lower(version(root).get("title"));
            Expression<String> description = cb.lower(cb.coalesce(version(root).get("shortDescription"), ""));
            Expression<String> instructor = cb.lower(cb.coalesce(root.get("instructor").get("fullName"), ""));
            return cb.or(
                    cb.like(title, pattern, '\\'),
                    cb.like(description, pattern, '\\'),
                    cb.like(instructor, pattern, '\\')
            );
        };
    }

    // Giữ khóa học thuộc một trong các danh mục cha được chọn.
    public static Specification<Course> hasCategoryIds(Collection<Long> categoryIds) {
        return (root, query, cb) ->
                (categoryIds == null || categoryIds.isEmpty())
                        ? null
                        : version(root).get("category").get("id").in(categoryIds);
    }

    // Giữ khóa học thuộc các danh mục con được chọn.
    public static Specification<Course> hasSubcategoryIds(Collection<Long> subcategoryIds) {
        return (root, query, cb) ->
                (subcategoryIds == null || subcategoryIds.isEmpty())
                        ? null
                        : version(root).get("subcategory").get("id").in(subcategoryIds);
    }

    public static Specification<Course> hasTopicId(Long topicId) {
        return (root, query, cb) -> {
            if (topicId == null) {
                return null;
            }
            query.distinct(true);
            return cb.equal(version(root).join("topics").get("id"), topicId);
        };
    }
    // Giữ khóa học có cấp độ nằm trong danh sách được chọn.
    public static Specification<Course> hasLevels(Collection<CourseLevel> levels) {
        return (root, query, cb) ->
                (levels == null || levels.isEmpty()) ? null : version(root).get("level").in(levels);
    }

    // Giữ khóa học có điểm trung bình đánh giá từ mức tối thiểu trở lên.
    public static Specification<Course> hasMinRating(Double minRating) {
        return (root, query, cb) -> {
            if (minRating == null) {
                return null;
            }
            Subquery<Double> average = query.subquery(Double.class);
            Root<CourseReview> review = average.from(CourseReview.class);
            average.select(cb.avg(review.get("rating")));
            average.where(cb.equal(review.get("course"), root));
            return cb.greaterThanOrEqualTo(average, minRating);
        };
    }

    // Lọc miễn phí, trả phí, hoặc nằm trong khoảng giá.
    public static Specification<Course> hasPriceFilter(String priceType, BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> pricePredicate(root, cb, priceType, min, max);
    }

    // Gộp toàn bộ điều kiện của trang tìm kiếm (chỉ khóa đã xuất bản) và nhúng luôn thứ tự sắp xếp,
    // bỏ qua khi query là câu đếm phân trang (getResultType() == Long/long).
    public static Specification<Course> buildSearchSpec(CourseSearchRequest req) {
        Specification<Course> filters = Specification.where(hasCourseStatus(CourseStatus.PUBLISHED))
                .and(hasPublishedVersion())
                .and(matchesKeyword(req.getKeyword()))
                .and(hasCategoryIds(req.getCategoryIds()))
                .and(hasSubcategoryIds(req.getSubcategoryIds()))
                .and(hasTopicId(req.getTopicId()))
                .and(hasLevels(req.getLevels()))
                .and(hasMinRating(req.getMinRating()))
                .and(hasPriceFilter(req.getPriceType(), req.getMin(), req.getMax()));

        return (root, query, cb) -> {
            Predicate predicate = filters.toPredicate(root, query, cb);
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                query.orderBy(buildOrders(req.getSort(), root, query, cb));
            }
            return predicate;
        };
    }

    // Dựng thứ tự sắp xếp: phổ biến, đánh giá, mới nhất, hoặc giá.
    private static List<Order> buildOrders(String sort, Root<Course> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Order> orders = new ArrayList<>();
        String normalized = sort == null ? "popular" : sort;
        switch (normalized) {
            case "rating" -> orders.add(cb.desc(averageRating(root, query, cb)));
            case "newest" -> orders.add(cb.desc(root.get("createdAt")));
            case "price_asc" -> orders.add(cb.asc(root.get("price")));
            case "price_desc" -> orders.add(cb.desc(root.get("price")));
            default -> {
                orders.add(cb.desc(enrollmentCount(root, query, cb)));
                orders.add(cb.desc(averageRating(root, query, cb)));
            }
        }
        if (!"newest".equals(normalized)) {
            orders.add(cb.desc(root.get("createdAt")));
        }
        orders.add(cb.desc(root.get("id")));
        return orders;
    }

    // Điểm trung bình review của khóa, khóa chưa có review được tính là 0.
    private static Expression<Double> averageRating(Root<Course> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Subquery<Double> subquery = query.subquery(Double.class);
        Root<CourseReview> review = subquery.from(CourseReview.class);
        subquery.select(cb.avg(review.get("rating")));
        subquery.where(cb.equal(review.get("course"), root));
        return cb.coalesce(subquery, 0.0);
    }

    // Số lượt ghi danh còn hiệu lực, không tính bản ghi đã thu hồi.
    private static Expression<Long> enrollmentCount(Root<Course> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Enrollment> enrollment = subquery.from(Enrollment.class);
        subquery.select(cb.count(enrollment));
        subquery.where(
                cb.equal(enrollment.get("course"), root),
                cb.notEqual(enrollment.get("status"), EnrollmentStatus.REVOKED)
        );
        return cb.coalesce(subquery, 0L);
    }

    // Biến từ khóa thành mẫu LIKE, coi % và _ trong từ khóa là ký tự thường.
    public static String likePattern(String keyword) {
        String escaped = keyword.trim()
                .toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    // Dựng điều kiện giá: FREE thì giá bằng 0, PAID thì giá lớn hơn 0, rồi áp min/max.
    private static Predicate pricePredicate(
            Root<Course> root,
            CriteriaBuilder cb,
            String priceType,
            BigDecimal min,
            BigDecimal max
    ) {
        List<Predicate> predicates = new ArrayList<>();
        if ("FREE".equals(priceType)) {
            predicates.add(cb.equal(root.get("price"), BigDecimal.ZERO));
            return cb.and(predicates.toArray(Predicate[]::new));
        }
        if ("PAID".equals(priceType)) {
            predicates.add(cb.greaterThan(root.get("price"), BigDecimal.ZERO));
        }
        if (min != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), min));
        }
        if (max != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), max));
        }
        if (predicates.isEmpty()) {
            return null;
        }
        return cb.and(predicates.toArray(Predicate[]::new));
    }
}
