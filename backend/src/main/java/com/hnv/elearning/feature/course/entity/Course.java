package com.hnv.elearning.feature.course.entity;

import com.hnv.elearning.feature.category.entity.Category;
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
import java.util.LinkedHashSet;
import java.util.List;
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

    @Column(unique = true, length = 255)
    private String slug;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private CourseStatus status;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private User instructor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_version_id")
    private CourseVersion publishedVersion;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


}
