package com.minhkhoi.swd392.mapper;

import com.minhkhoi.swd392.dto.request.CreateModuleRequest;
import com.minhkhoi.swd392.dto.response.ModuleResponse;
import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.entity.Module;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ModuleMapper {

    @Mapping(target = "moduleId", ignore = true)
    @Mapping(target = "title", source = "request.title")
    @Mapping(target = "course", source = "course")
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "orderIndex", source = "request.orderIndex")
    @Mapping(target = "isPending", ignore = true)
    @Mapping(target = "isPendingDeletion", ignore = true)
    Module toModule(CreateModuleRequest request, Course course);

    @Mapping(target = "lessons", source = "lessons")
    ModuleResponse toModuleResponse(Module module);
}
