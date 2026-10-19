package com.hnv.elearning.feature.course.repository;

import com.hnv.elearning.feature.course.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    interface QuizQuestionCount {
        Long getQuizId();

        Long getTotal();
    }

    // Đếm câu hỏi của nhiều quiz trong một query. Quiz chưa có câu hỏi nào không có dòng trong kết quả.
    // quizIds không được rỗng: caller phải bỏ qua lời gọi khi không có quiz.
    @Query("""
            SELECT q.quiz.id AS quizId, COUNT(q) AS total
            FROM Question q
            WHERE q.quiz.id IN :quizIds
            GROUP BY q.quiz.id
            """)
    List<QuizQuestionCount> countByQuizIds(@Param("quizIds") Collection<Long> quizIds);
}
