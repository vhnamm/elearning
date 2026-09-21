package com.hnv.elearning.feature.enrollment.entity;

import com.hnv.elearning.feature.course.entity.Course;
import com.hnv.elearning.feature.enrollment.enums.EnrollmentStatus;
import com.hnv.elearning.feature.order.entity.OrderItem;
import com.hnv.elearning.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;

    @Column(name = "enrolled_at")
    @CreationTimestamp
    private LocalDateTime enrolledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem; //tracibility -> webhook bắn ipn về tránh insert truùng 2 enroll cho 1 course
}
