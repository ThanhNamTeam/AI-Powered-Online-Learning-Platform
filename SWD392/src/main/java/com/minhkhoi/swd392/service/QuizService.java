package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.dto.request.QuizSubmissionRequest;
import com.minhkhoi.swd392.dto.response.QuizSubmissionResponse;
import com.minhkhoi.swd392.entity.*;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.repository.QuizRepository;
import com.minhkhoi.swd392.repository.QuizResultRepository;
import com.minhkhoi.swd392.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;

    @Transactional
    public QuizSubmissionResponse submitQuiz(QuizSubmissionRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));

        List<Question> questions = quiz.getQuestions();
        if (questions == null || questions.isEmpty()) {
            throw new AppException(ErrorCode.QUESTION_NOT_FOUND);
        }

        Map<UUID, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getQuestionId, q -> q));

        int correctAnswersCount = 0;
        List<QuizSubmissionResponse.QuestionReviewDetail> details = new ArrayList<>();

        for (QuizSubmissionRequest.QuizAnswerItem answerItem : request.getAnswers()) {
            Question question = questionMap.get(answerItem.getQuestionId());
            if (question == null) continue;

            boolean isCorrect = question.getCorrectAnswer().equalsIgnoreCase(answerItem.getSelectedAnswer());
            if (isCorrect) {
                correctAnswersCount++;
            }

            details.add(QuizSubmissionResponse.QuestionReviewDetail.builder()
                    .questionId(question.getQuestionId())
                    .questionContent(question.getContent())
                    .yourAnswer(answerItem.getSelectedAnswer())
                    .correctAnswer(question.getCorrectAnswer())
                    .explanation(question.getExplanation())
                    .isCorrect(isCorrect)
                    .build());
        }

        double score = (double) correctAnswersCount / questions.size() * 10.0;

        QuizResult quizResult = QuizResult.builder()
                .user(user)
                .quiz(quiz)
                .score(score)
                .correctAnswersCount(correctAnswersCount)
                .totalQuestionsCount(questions.size())
                .build();

        quizResultRepository.save(quizResult);

        return QuizSubmissionResponse.builder()
                .quizId(quiz.getQuizId())
                .quizTitle(quiz.getTitle())
                .totalQuestions(questions.size())
                .correctAnswersCount(correctAnswersCount)
                .score(score)
                .questionDetails(details)
                .build();
    }
}
