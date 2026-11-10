package com.hnv.elearning.feature.course.repository;

import com.hnv.elearning.feature.course.entity.Question;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    interface QuizQuestionCount {
        Long getQuizId();

        Long getTotal();
    }


    @Query("""
            SELECT q.quiz.id AS quizId, COUNT(q) AS total
            FROM Question q
            WHERE q.quiz.id IN :quizIds
            GROUP BY q.quiz.id
            """)
    List<QuizQuestionCount> countByQuizIds(@Param("quizIds") Collection<Long> quizIds);

    @Query("""
        SELECT COALESCE(MAX(ques.position), 0)  FROM Question ques WHERE ques.quiz.id = :quizId
    """)
    Integer findMaxQuestionPositionByQuizId(@Param("quizId") Long quizId);

    @EntityGraph(attributePaths = {"quiz", "options"})
    List<Question> findByQuizIdOrderByPositionAsc(Long quizId);

    @Query("""
        SELECT q FROM Question q JOIN FETCH q.options WHERE q.id = :id
    """)
    Optional<Question> findByIdWithAnser(@Param("id") Long id);
}
