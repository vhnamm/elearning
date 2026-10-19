package com.hnv.elearning.feature.course.entity;

import com.hnv.elearning.feature.category.entity.Category;
import com.hnv.elearning.feature.category.entity.Subcategory;
import com.hnv.elearning.feature.category.entity.Topic;
import com.hnv.elearning.feature.course.enums.CourseLevel;
import com.hnv.elearning.feature.course.enums.CourseVersionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "course_versions")
public class CourseVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Enumerated(EnumType.STRING)
    private CourseVersionStatus status;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Enumerated(EnumType.STRING)
    private CourseLevel level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id")
    private Subcategory subcategory;

    @Column(name = "thumbnail_key", length = 1024)
    private String thumbnailKey;

    @OneToMany(mappedBy = "courseVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CourseLearningOutcome> learningOutcomes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "courseVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CourseRequiredSkill>  requiredSkills = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "course_version_topics",
        joinColumns = @JoinColumn(name = "course_version_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    @Builder.Default
    private Set<Topic> topics = new LinkedHashSet<>();

    @OneToMany(mappedBy = "courseVersion", cascade =  CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("position ASC")
    private Set<Section> sections = new LinkedHashSet<>();

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    //Helper
    public void replaceOutcomes(List<String> newOutcomes){
        this.learningOutcomes.clear();
        for (String outcome : newOutcomes) {
            CourseLearningOutcome courseLearningOutcome = new CourseLearningOutcome();
            courseLearningOutcome.setCourseVersion(this);
            courseLearningOutcome.setContent(outcome);
            this.learningOutcomes.add(courseLearningOutcome);
        }
    }

    public void replaceSkills(List<String> newSkills){
        this.requiredSkills.clear();
        for (String skill : newSkills) {
            CourseRequiredSkill courseRequiredSkill = new CourseRequiredSkill();
            courseRequiredSkill.setCourseVersion(this);
            courseRequiredSkill.setContent(skill);

            this.requiredSkills.add(courseRequiredSkill);
        }
    }
}
