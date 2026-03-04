package com.minhkhoi.swd392.mapper;

import com.minhkhoi.swd392.dto.request.CreateLessonRequest;
import com.minhkhoi.swd392.dto.response.LessonResponse;
import com.minhkhoi.swd392.entity.Lesson;
import com.minhkhoi.swd392.entity.Module;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LessonMapper {

    @Mapping(target = "lessonId", ignore = true)
    @Mapping(target = "title", source = "request.title")
    @Mapping(target = "module", source = "module")
    @Mapping(target = "videoUrl", ignore = true)
    @Mapping(target = "documentUrl", ignore = true)
    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "documentContent", ignore = true)
    @Mapping(target = "transcript", ignore = true)
    @Mapping(target = "quizStatus", ignore = true)
    @Mapping(target = "quizzes", ignore = true)
    @Mapping(target = "lastQuizError", ignore = true)
    @Mapping(target = "vectorStores", ignore = true)
    @Mapping(target = "progressList", ignore = true)
    Lesson toLesson(CreateLessonRequest request, Module module);


    LessonResponse toLessonResponse(Lesson lesson);
}
