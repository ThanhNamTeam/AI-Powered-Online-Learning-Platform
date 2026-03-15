package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.constant.CourseStatus;
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
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        validateCourseMutable(course);

        Module module = moduleMapper.toModule(request, course);
        
        if (course.getStatus() == CourseStatus.EDITING) {
            module.setIsPending(true);
        }
        
        module = moduleRepository.save(module);

        return moduleMapper.toModuleResponse(module);
    }

    public List<ModuleResponse> getModulesByCourse(UUID courseId) {
        return moduleRepository.findByCourse_CourseIdOrderByOrderIndexAsc(courseId).stream()
                .map(moduleMapper::toModuleResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ModuleResponse updateModule(UUID moduleId, String title, Integer orderIndex) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new AppException(ErrorCode.MODULE_NOT_FOUND));
        
        validateCourseMutable(module.getCourse());

        if (title != null && !title.trim().isEmpty()) {
            module.setTitle(title);
        }
        if (orderIndex != null) {
            module.setOrderIndex(orderIndex);
        }

        Module updated = moduleRepository.save(module);
        return moduleMapper.toModuleResponse(updated);
    }

    @Transactional
    public void deleteModule(UUID moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new AppException(ErrorCode.MODULE_NOT_FOUND));

        validateCourseMutable(module.getCourse());

        if (module.getCourse().getStatus() == CourseStatus.EDITING) {
            module.setIsPendingDeletion(true);
            moduleRepository.save(module);
        } else {
            moduleRepository.delete(module);
        }
    }

    private void validateCourseMutable(Course course) {
        CourseStatus status = course.getStatus();
        if (status != CourseStatus.DRAFT && status != CourseStatus.EDITING && status != CourseStatus.REJECTED) {
            throw new AppException(ErrorCode.INVALID_COURSE_STATUS_FOR_UPDATE);
        }
    }
}
