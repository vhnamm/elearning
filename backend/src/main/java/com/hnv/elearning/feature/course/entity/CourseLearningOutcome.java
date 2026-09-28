package com.hnv.elearning.feature.course.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_learning_outcomes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CourseLearningOutcome {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "content", nullable = false, length = 1024)
    private String content;
}
