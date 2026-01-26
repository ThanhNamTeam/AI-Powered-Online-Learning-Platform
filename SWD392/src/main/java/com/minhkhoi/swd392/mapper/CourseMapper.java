package com.minhkhoi.swd392.mapper;

import com.minhkhoi.swd392.dto.request.CreateCourseRequest;
import com.minhkhoi.swd392.dto.response.CourseResponse;
import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "courseId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "constructor", source = "constructor")
    @Mapping(target = "modules", ignore = true)
    @Mapping(target = "enrollments", ignore = true)
    @Mapping(target = "reports", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    Course toCourse(CreateCourseRequest request, User constructor);

    @Mapping(target = "constructorId", source = "constructor.userId")
    @Mapping(target = "constructorName", source = "constructor.fullName")
    CourseResponse toCourseResponse(Course course);
}
