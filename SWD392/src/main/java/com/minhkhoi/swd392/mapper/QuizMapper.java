package com.minhkhoi.swd392.mapper;

import com.minhkhoi.swd392.dto.response.QuestionResponse;
import com.minhkhoi.swd392.dto.response.QuizResponse;
import com.minhkhoi.swd392.entity.Question;
import com.minhkhoi.swd392.entity.Quiz;
import com.minhkhoi.swd392.service.LessonAsyncService.QuestionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QuizMapper {

    @Mapping(target = "lessonId", source = "lesson.lessonId")
    @Mapping(target = "lessonTitle", source = "lesson.title")
    @Mapping(target = "totalQuestions", expression = "java(quiz.getQuestions() != null ? quiz.getQuestions().size() : 0)")
    QuizResponse toQuizResponse(Quiz quiz);

    QuestionResponse toQuestionResponse(Question question);

    List<QuestionResponse> toQuestionResponseList(List<Question> questions);

    @Mapping(target = "questionId", ignore = true)
    @Mapping(target = "quiz", ignore = true)
    Question toQuestion(QuestionDTO dto);
}
