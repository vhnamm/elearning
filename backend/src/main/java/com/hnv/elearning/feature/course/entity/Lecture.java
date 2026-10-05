package com.hnv.elearning.feature.course.entity;

import com.hnv.elearning.feature.course.enums.LectureType;
import com.hnv.elearning.feature.course.enums.LectureVideoStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lectures")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lecture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_item_id")
    private CurriculumItem curriculumItem;

    @Enumerated(EnumType.STRING)
    private LectureType contentType;

    @Enumerated(EnumType.STRING)
    private LectureVideoStatus videoStatus;

    @Column(name = "video_raw_key", length = 1024)
    private String videoRawKey;

    @Column(name="video_key", length = 1024)
    private String videoKey;

    @Column(name = "video_duration_seconds")
    private Integer videoDurationSeconds;

    @Column(name = "thumbnail_url", length = 1024)
    private String thumbnailUrl;
}