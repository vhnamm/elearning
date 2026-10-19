package com.hnv.elearning.feature.course.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "quizzes")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Quizz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_item_id")
    private CurriculumItem curriculumItem;

    @Column(name = "passing_score")
    private Integer passingScore;

    @OneToMany(mappedBy = "quiz",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;
}