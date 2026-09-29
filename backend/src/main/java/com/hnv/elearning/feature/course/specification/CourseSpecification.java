package com.hnv.elearning.feature.course.specification;

import com.hnv.elearning.feature.course.dto.CourseSearchRequest;
import com.hnv.elearning.feature.course.entity.Course;
import com.hnv.elearning.feature.course.enums.CourseLevel;
import com.hnv.elearning.feature.course.enums.CourseStatus;
import com.hnv.elearning.feature.enrollment.entity.Enrollment;
import com.hnv.elearning.feature.enrollment.enums.EnrollmentStatus;
import com.hnv.elearning.feature.review.entity.CourseReview;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
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
    public static Specification<Course> hasInstructorId(Long id){
        return (root, query, cb) -> id == null ? null :  cb.equal(root.get("instructor").get("id"), id);
    }

    public static Specification<Course> hasCourseStatus(CourseStatus status){
        return (root, query, cb) ->
                status == null ? null :
                cb.equal(root.get("status"), status);
    }

    public static Specification<Course> hasCourseTitle(String title){
        return (root, query, cb) ->
                (title == null || title.isBlank()) ? null :
                        cb.like(cb.lower(root.get("title")), "%" +  title.toLowerCase() + "%");
    }

    public static Specification<Course> priceBetween(BigDecimal min, BigDecimal max){
        return (root, query, cb) -> {
            if(min == null && max == null) return null;
            if(min != null && max != null) return cb.between(root.get("price"), min, max);
            if(min == null && max != null) return cb.lessThanOrEqualTo(root.get("price"), max);
            else return cb.greaterThanOrEqualTo(root.get("price"), min);
        };
    }

    //custom cho actor cu the
    public static Specification<Course> forInstructor(Long instructorId, CourseStatus status, String keyword){
        return Specification.where(hasInstructorId(instructorId))
                .and(hasCourseStatus(status))
                .and(hasCourseTitle(keyword));
    }

    public static Specification<Course> forPublic(String keyword, BigDecimal min, BigDecimal max){
        return Specification.where(hasCourseStatus(CourseStatus.PUBLISHED))
                .and(hasCourseTitle(keyword))
                .and(priceBetween(min, max));
    }

    // Khớp từ khóa với tên khóa, mô tả ngắn và giảng viên. Không còn khớp theo tên danh mục/danh mục con.
    public static Specification<Course> matchesKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            String pattern = likePattern(keyword);
            Expression<String> title = cb.lower(root.get("title"));
            Expression<String> description = cb.lower(cb.coalesce(root.get("shortDescription"), ""));
            Expression<String> instructor = cb.lower(cb.coalesce(root.get("instructor").get("fullName"), ""));
            return cb.or(
                    cb.like(title, pattern, '\\'),
                    cb.like(description, pattern, '\\'),
                    cb.like(instructor, pattern, '\\')
            );
        };
    }

    // Giữ khóa học thuộc danh mục cha được chọn. Mỗi khóa chỉ thuộc 1 danh mục nên chỉ lọc theo 1 id.
    public static Specification<Course> hasCategoryId(Long categoryId) {
        return (root, query, cb) ->
                categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    // Giữ khóa học thuộc các danh mục con được chọn.
    public static Specification<Course> hasSubcategoryIds(Collection<Long> subcategoryIds) {
        return (root, query, cb) ->
                (subcategoryIds == null || subcategoryIds.isEmpty())
                        ? null
                        : root.get("subcategory").get("id").in(subcategoryIds);
    }

    // Giữ khóa học có cấp độ nằm trong danh sách được chọn.
    public static Specification<Course> hasLevels(Collection<CourseLevel> levels) {
        return (root, query, cb) ->
                (levels == null || levels.isEmpty()) ? null : root.get("level").in(levels);
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
                .and(matchesKeyword(req.getKeyword()))
                .and(hasCategoryId(req.getCategoryId()))
                .and(hasSubcategoryIds(req.getSubcategoryIds()))
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
