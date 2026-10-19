package com.hnv.elearning.feature.course.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_required_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CourseRequiredSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_version_id", nullable = false)
    private CourseVersion courseVersion;

    @Column(name = "content", nullable = false, length = 1024)
    private String content;

}
