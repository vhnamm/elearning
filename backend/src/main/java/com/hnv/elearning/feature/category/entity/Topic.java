package com.hnv.elearning.feature.category.entity;

import com.hnv.elearning.feature.category.enums.TopicStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "topics")
@Getter
@Setter
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String slug;

    @Enumerated(EnumType.STRING)
    private TopicStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id")
    private Subcategory subcategory;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
