package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.dto.request.CreateModuleRequest;
import com.minhkhoi.swd392.dto.response.ModuleResponse;
import com.minhkhoi.swd392.entity.Course;
import com.minhkhoi.swd392.entity.Module;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.repository.CourseRepository;
import com.minhkhoi.swd392.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public ModuleResponse createModule(CreateModuleRequest request) {
        log.info("Creating new module: {} for course: {}", request.getTitle(), request.getCourseId());

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        Module module = Module.builder()
                .title(request.getTitle())
                .course(course)
                .orderIndex(request.getOrderIndex())
                .build();

        module = moduleRepository.save(module);

        return ModuleResponse.builder()
                .moduleId(module.getModuleId())
                .title(module.getTitle())
                .orderIndex(module.getOrderIndex())
                .build();
    }

    public List<ModuleResponse> getModulesByCourse(UUID courseId) {
        return moduleRepository.findByCourse_CourseIdOrderByOrderIndexAsc(courseId).stream()
                .map(m -> ModuleResponse.builder()
                        .moduleId(m.getModuleId())
                        .title(m.getTitle())
                        .orderIndex(m.getOrderIndex())
                        .build())
                .collect(Collectors.toList());
    }
}
