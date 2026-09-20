package com.hnv.elearning.feature.moderation.entity;

import com.hnv.elearning.feature.course.entity.Course;
import com.hnv.elearning.feature.moderation.enums.ModerationAction;
import com.hnv.elearning.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

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
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id")
    private User moderator;

    @Enumerated(EnumType.STRING)
    private ModerationAction action;

    private String feedback;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
