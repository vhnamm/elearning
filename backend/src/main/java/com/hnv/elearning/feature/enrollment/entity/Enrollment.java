package com.hnv.elearning.feature.enrollment.entity;

import com.hnv.elearning.feature.course.entity.Course;
import com.hnv.elearning.feature.user.entity.User;
import jakarta.persistence.*;

@Entity
@Table(name = "enrollments")
public class Enrollment {
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Course course;

    private
}
