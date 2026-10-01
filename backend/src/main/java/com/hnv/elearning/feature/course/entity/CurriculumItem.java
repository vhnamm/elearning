package com.hnv.elearning.feature.course.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.hnv.elearning.feature.course.enums.CurriculumItemType;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
@Entity
@Table(name = "curriculum_items")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CurriculumItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @Enumerated(EnumType.STRING)
    private CurriculumItemType type;

    private String title;

    private int position;

    @Column(name = "is_preview")
    private boolean isPreview;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "curriculumItem", cascade = CascadeType.ALL)
    private Lecture lecture;

    @OneToOne(mappedBy = "curriculumItem", cascade = CascadeType.ALL)
    private Quizz quizz;
}