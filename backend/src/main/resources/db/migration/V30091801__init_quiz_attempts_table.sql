CREATE TABLE quiz_attempts (
     id           BIGINT    NOT NULL AUTO_INCREMENT,
     quiz_id      BIGINT    NOT NULL,
     user_id      BIGINT    NOT NULL,
     score        INT       NULL,
     passed       BOOLEAN   NOT NULL DEFAULT FALSE,
     started_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     submitted_at TIMESTAMP NULL,
     PRIMARY KEY (id),
     KEY idx_quiz_attempts_user_quiz (user_id, quiz_id),
     CONSTRAINT fk_quiz_attempts_quiz
         FOREIGN KEY (quiz_id) REFERENCES quizzes (id) ON DELETE CASCADE,
     CONSTRAINT fk_quiz_attempts_user
         FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE quiz_answers (
     id                 BIGINT  NOT NULL AUTO_INCREMENT,
     attempt_id         BIGINT  NOT NULL,
     question_id        BIGINT  NOT NULL,
     selected_option_id BIGINT  NULL,
     is_correct         BOOLEAN NULL,
     PRIMARY KEY (id),
     UNIQUE KEY uq_quiz_answers_attempt_question (attempt_id, question_id),
     CONSTRAINT fk_quiz_answers_attempt
         FOREIGN KEY (attempt_id) REFERENCES quiz_attempts (id) ON DELETE CASCADE,
     CONSTRAINT fk_quiz_answers_question
         FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE CASCADE,
     CONSTRAINT fk_quiz_answers_option
         FOREIGN KEY (selected_option_id) REFERENCES question_options (id) ON DELETE SET NULL
) ENGINE=InnoDB;
