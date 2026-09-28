package com.hnv.elearning.feature.course.entity;

import com.hnv.elearning.feature.category.entity.Subcategory;
import com.hnv.elearning.feature.category.entity.Topic;
import com.hnv.elearning.feature.course.enums.CourseLevel;
import com.hnv.elearning.feature.course.enums.CourseStatus;
import com.hnv.elearning.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "courses")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 255, nullable = false)
    private String slug;
    @Column(length = 255, nullable = false)
    private String title;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url", length = 1024, nullable = false)
    private String thumbnailUrl;


    @Column(nullable = false)
    private BigDecimal price;


    @Enumerated(EnumType.STRING)
    private CourseStatus status;

    @Enumerated(EnumType.STRING)
    private CourseLevel level;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "instructor_id")
    private User instructor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id", nullable = false)
    private Subcategory subcategory;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "course_topics",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    @Builder.Default
    private Set<Topic> topics = new HashSet<>();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
