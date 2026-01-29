package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.dto.request.CreateModuleRequest;
import com.minhkhoi.swd392.dto.response.ModuleResponse;
import com.minhkhoi.swd392.mapper.ModuleMapper;
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
    private final ModuleMapper moduleMapper;

    @Transactional
    public ModuleResponse createModule(CreateModuleRequest request) {
        log.info("Creating new module: {} for course: {}", request.getTitle(), request.getCourseId());

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        Module module = moduleMapper.toModule(request, course);

        module = moduleRepository.save(module);

        return moduleMapper.toModuleResponse(module);
    }

    public List<ModuleResponse> getModulesByCourse(UUID courseId) {
        return moduleRepository.findByCourse_CourseIdOrderByOrderIndexAsc(courseId).stream()
                .map(moduleMapper::toModuleResponse)
                .collect(Collectors.toList());
    }
}
