package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.dto.request.ReviewRequest;
import com.minhkhoi.swd392.entity.*;
import com.minhkhoi.swd392.repository.CourseRepository;
import com.minhkhoi.swd392.repository.EnrollmentRepository;
import com.minhkhoi.swd392.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final GeminiService geminiService;

    public void submitReview(User user, ReviewRequest request) {
        Enrollment enrollment = enrollmentRepository.findByUserAndCourse(user, 
                courseRepository.findById(request.getCourseId())
                        .orElseThrow(() -> new com.minhkhoi.swd392.exception.AppException(com.minhkhoi.swd392.exception.ErrorCode.COURSE_NOT_FOUND)))
                .orElseThrow(() -> new com.minhkhoi.swd392.exception.AppException(com.minhkhoi.swd392.exception.ErrorCode.ENROLLMENT_NOT_FOUND));

        if (enrollment.getStatus() != com.minhkhoi.swd392.constant.EnrollmentStatus.COMPLETED) {
            throw new com.minhkhoi.swd392.exception.AppException(com.minhkhoi.swd392.exception.ErrorCode.COURSE_NOT_COMPLETED);
        }

        moderateComment(request.getComment());

        Review review = reviewRepository.findByUserUserIdAndCourseCourseId(user.getUserId(), request.getCourseId())
                .orElse(new Review());
        
        review.setUser(user);
        review.setCourse(enrollment.getCourse());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        reviewRepository.save(review);
    }

    private void moderateComment(String comment) {
        String prompt = "Bạn là một chuyên gia kiểm duyệt nội dung. Hãy kiểm tra bình luận sau đây có chứa ngôn từ xúc phạm, thô tục, không đúng chuẩn mực đạo đức hoặc vi phạm quy tắc cộng đồng không. " +
                "Chỉ trả lời một từ duy nhất: 'PASSED' nếu hợp lệ, hoặc 'REJECTED' nếu không hợp lệ. " +
                "Bình luận: \"" + comment + "\"";
        
        try {
            String result = geminiService.callGeminiWithPrompt(prompt).trim().toUpperCase();
            if (result.contains("REJECTED")) {
                throw new RuntimeException("Bình luận của bạn chứa nội dung không phù hợp và bị từ chối bởi hệ thống kiểm duyệt AI.");
            }
        } catch (Exception e) {
            log.error("AI Moderation failed: {}", e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("Bình luận của bạn chứa nội dung không phù hợp")) {
                throw (RuntimeException) e;
            }
        }
    }
}
