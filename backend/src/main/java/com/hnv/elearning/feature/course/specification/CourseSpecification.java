package com.hnv.elearning.feature.course.specification;

import com.hnv.elearning.feature.course.entity.Course;
import com.hnv.elearning.feature.course.enums.CourseLevel;
import com.hnv.elearning.feature.course.enums.CourseStatus;
import com.hnv.elearning.feature.review.entity.CourseReview;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
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

    public static Specification<Course> matchesKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            String pattern = likePattern(keyword);
            Expression<String> title = cb.lower(root.get("title"));
            Expression<String> description = cb.lower(cb.coalesce(root.get("shortDescription"), ""));
            Expression<String> instructor = cb.lower(cb.coalesce(root.get("instructor").get("fullName"), ""));
            Expression<String> subcategory = cb.lower(root.get("subcategory").get("name"));
            Expression<String> category = cb.lower(root.get("subcategory").get("category").get("name"));
            return cb.or(
                    cb.like(title, pattern, '\\'),
                    cb.like(description, pattern, '\\'),
                    cb.like(instructor, pattern, '\\'),
                    cb.like(subcategory, pattern, '\\'),
                    cb.like(category, pattern, '\\')
            );
        };
    }

    public static Specification<Course> hasCategoryIds(Collection<Long> categoryIds) {
        return (root, query, cb) ->
                (categoryIds == null || categoryIds.isEmpty())
                        ? null
                        : root.get("subcategory").get("category").get("id").in(categoryIds);
    }

    public static Specification<Course> hasLevels(Collection<CourseLevel> levels) {
        return (root, query, cb) ->
                (levels == null || levels.isEmpty()) ? null : root.get("level").in(levels);
    }

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

    public static Specification<Course> hasPriceFilter(String priceType, BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> pricePredicate(root, cb, priceType, min, max);
    }

    public static Specification<Course> forSearch(
            String keyword,
            Collection<Long> categoryIds,
            Collection<CourseLevel> levels,
            Double minRating,
            String priceType,
            BigDecimal min,
            BigDecimal max
    ) {
        return Specification.where(hasCourseStatus(CourseStatus.PUBLISHED))
                .and(matchesKeyword(keyword))
                .and(hasCategoryIds(categoryIds))
                .and(hasLevels(levels))
                .and(hasMinRating(minRating))
                .and(hasPriceFilter(priceType, min, max));
    }

    public static String likePattern(String keyword) {
        String escaped = keyword.trim()
                .toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

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
