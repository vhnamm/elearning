CREATE TABLE questions (
     id          BIGINT NOT NULL AUTO_INCREMENT,
     quiz_id     BIGINT NOT NULL,
     content     TEXT   NOT NULL,
     position    INT    NOT NULL DEFAULT 0,
     PRIMARY KEY (id),
     KEY idx_questions_quiz (quiz_id, position),
     CONSTRAINT fk_questions_quiz
         FOREIGN KEY (quiz_id) REFERENCES quizzes (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- position 0-3 tương ứng đáp án A-D (mỗi câu tối đa 4 đáp án)
CREATE TABLE question_options (
     id          BIGINT       NOT NULL AUTO_INCREMENT,
     question_id BIGINT       NOT NULL,
     content     VARCHAR(500) NOT NULL,
     is_correct  BOOLEAN      NOT NULL DEFAULT FALSE,
     position    INT          NOT NULL,
     PRIMARY KEY (id),
     UNIQUE KEY uq_question_options_question_position (question_id, position),
     CONSTRAINT chk_question_options_position CHECK (position BETWEEN 0 AND 3),
     CONSTRAINT fk_question_options_question
         FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE CASCADE
) ENGINE=InnoDB;
