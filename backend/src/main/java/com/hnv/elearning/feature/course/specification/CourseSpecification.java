package com.hnv.elearning.feature.course.specification;

import com.hnv.elearning.feature.course.entity.Course;
import com.hnv.elearning.feature.course.enums.CourseStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

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
}
