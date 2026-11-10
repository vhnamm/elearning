package com.hnv.elearning.feature.course.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer position;

    @Builder.Default
    @OrderBy("position")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "question")
    private List<QuestionOption> options = new ArrayList<>();

    public void addOption(QuestionOption option){
        option.setPosition(options.size());
        options.add(option);
        option.setQuestion(this);
    }

    public void clearOptions() {
        options.clear();   // orphanRemoval sẽ xóa các dòng đáp án cũ khỏi DB
    }
}
