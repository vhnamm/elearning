package com.hnv.elearning.feature.moderation.entity;

import com.hnv.elearning.feature.course.entity.Course;
import com.hnv.elearning.feature.moderation.enums.ModerationAction;
import com.hnv.elearning.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_moderation_logs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseModerationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id", nullable = false)
    private User moderator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModerationAction action;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String feedback;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

}
