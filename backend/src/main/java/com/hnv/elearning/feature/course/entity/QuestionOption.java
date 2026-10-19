package com.hnv.elearning.feature.course.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "question_options")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(length = 500)
    private String content;

    @Column(name = "is_correct")
    private boolean isCorrect;

    // 0-3 tương ứng đáp án A-D. BE tự gán theo thứ tự mảng request, không nhận từ FE.
    private int position;
}
