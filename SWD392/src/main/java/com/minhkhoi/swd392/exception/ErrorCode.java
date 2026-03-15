package com.minhkhoi.swd392.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    USER_NOT_FOUND("User not found with email: %s", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS("Email already exists: %s", HttpStatus.CONFLICT),
    EMAIL_NOT_VERIFIED("Email has not been verified", HttpStatus.BAD_REQUEST),


    INVALID_CREDENTIALS("Mật khẩu hoặc email không chính xác", HttpStatus.UNAUTHORIZED),
    WRONG_OLD_PASSWORD("Mật khẩu hiện tại không đúng vui lòng nhập lại  ", HttpStatus.BAD_REQUEST),

    EQUAL_PASSWORD("Mật khẩu mới không được trùng với mật khẩu hiện tại", HttpStatus.BAD_REQUEST),

    CONFIRM_PASSWORD_MISMATCH("Mật khẩu xác nhận không khớp", HttpStatus.BAD_REQUEST),

    UNAUTHORIZED("Unauthorized access", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("Access denied", HttpStatus.FORBIDDEN),
    REFRESH_TOKEN_INVALID("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED),


    OTP_EXPIRED("OTP has expired", HttpStatus.BAD_REQUEST),
    OTP_INVALID("Invalid OTP code", HttpStatus.BAD_REQUEST),
    OTP_SEND_FAILED("Failed to send OTP email", HttpStatus.INTERNAL_SERVER_ERROR),


    VALIDATION_ERROR("Validation failed", HttpStatus.BAD_REQUEST),
    INVALID_INPUT("Invalid input data", HttpStatus.BAD_REQUEST),


    INTERNAL_SERVER_ERROR("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNCATEGORIZED_ERROR("Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),


    FILE_UPLOAD_FAILED("Failed to upload file to Cloudinary", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_FAILED("Failed to delete file from Cloudinary", HttpStatus.INTERNAL_SERVER_ERROR),
    TRANSCRIPTION_FAILED("Failed to transcribe video", HttpStatus.INTERNAL_SERVER_ERROR),
    TRANSCRIPT_PARSE_FAILED("Failed to parse transcript response", HttpStatus.INTERNAL_SERVER_ERROR),

    INVALID_FILE("Invalid file format or size", HttpStatus.BAD_REQUEST),
    VIDEO_REQUIRED("Video file is required for lesson creation", HttpStatus.BAD_REQUEST),
    DOCUMENT_NOT_PDF("Document must be a PDF file", HttpStatus.BAD_REQUEST),
    DOCUMENT_NOT_FOUND("No document attached to this lesson", HttpStatus.NOT_FOUND),
    AUDIO_INVALID("Invalid audio file. Supported formats: MP3, WAV, M4A, OGG. Max size: 50MB", HttpStatus.BAD_REQUEST),
    PLACEMENT_DOCUMENT_NOT_FOUND("Placement document not found", HttpStatus.NOT_FOUND),


    COURSE_NOT_FOUND("Course not found", HttpStatus.NOT_FOUND),
    MODULE_NOT_FOUND("Module not found", HttpStatus.NOT_FOUND),
    LESSON_NOT_FOUND("Lesson not found", HttpStatus.NOT_FOUND),
    COURSE_CREATION_FAILED("Failed to create course", HttpStatus.INTERNAL_SERVER_ERROR),
    MISSING_COURSE_TITLE("Course title is required", HttpStatus.BAD_REQUEST),
    INVALID_PRICE("Price must be greater than or equal to 0", HttpStatus.BAD_REQUEST),
    MISSING_REJECTION_REASON("Rejection reason is required when rejecting a course", HttpStatus.BAD_REQUEST),
    INVALID_VERIFY_STATUS("Only PUBLISHED or REJECTED status is allowed for verification", HttpStatus.BAD_REQUEST),


    QUIZ_NOT_FOUND("Quiz not found", HttpStatus.NOT_FOUND),
    PREMIUM_REQUIRED("Bạn cần mua gói PREMIUM để sử dụng tính năng tạo câu hỏi bằng AI. Vui lòng nâng cấp tài khoản!", HttpStatus.PAYMENT_REQUIRED),
    QUIZ_ALREADY_EXISTS("The quiz already exists", HttpStatus.BAD_REQUEST),
    QUIZ_GENERATION_IN_PROGRESS("The system is processing, please wait", HttpStatus.ACCEPTED),
    MIN_MODULES_REQUIRED("Course must have at least 3 modules to request approval", HttpStatus.BAD_REQUEST),
    COURSE_NOT_APPROVED("Course must be APPROVED to generate quiz", HttpStatus.BAD_REQUEST),
    INVALID_CREATE_STATUS("Only DRAFT status is allowed for creation", HttpStatus.BAD_REQUEST),


    PAYMENT_NOT_FOUND("Payment not found", HttpStatus.NOT_FOUND),
    INVALID_SUBSCRIPTION_PLAN("Invalid subscription plan. Must be BASIC, PREMIUM, or ENTERPRISE", HttpStatus.BAD_REQUEST),
    ONLY_STUDENT_OR_INSTRUCTOR_CAN_PURCHASE("Only INSTRUCTOR or STUDENT can purchase premium subscriptions", HttpStatus.FORBIDDEN),
    PAYMENT_CREATION_FAILED("Failed to create payment: %s", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_MOMO_SIGNATURE("Invalid MOMO signature", HttpStatus.BAD_REQUEST),
    MOMO_CALLBACK_ERROR("Error handling MOMO callback", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_VNPAY_SIGNATURE("Invalid VNPAY signature", HttpStatus.BAD_REQUEST),
    VNPAY_CALLBACK_ERROR("Error handling VNPAY callback", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_COURSE_STATUS_FOR_APPROVAL("Course must be in PENDING_APPROVAL status to be verified", HttpStatus.BAD_REQUEST),
    ENROLLMENT_NOT_FOUND("Bạn chưa đăng ký khóa học này", HttpStatus.NOT_FOUND),
    QUESTION_NOT_FOUND("Question not found", HttpStatus.NOT_FOUND),
    REPORT_NOT_FOUND("Report not found", HttpStatus.NOT_FOUND),
    DISCUSSION_NOT_FOUND("Discussion not found", HttpStatus.NOT_FOUND),
    REVIEW_NOT_FOUND("Review not found", HttpStatus.NOT_FOUND),
    INVALID_COURSE_STATUS_FOR_UPDATE("Course must be APPROVED to submit an update request", HttpStatus.BAD_REQUEST),
    INVALID_COURSE_STATUS_FOR_DELETION("Course must be APPROVED or PENDING_UPDATE to request deletion", HttpStatus.BAD_REQUEST),


    SUBSCRIPTION_EXPIRED("Gói hiện tại của bạn đã hết hạn", HttpStatus.BAD_REQUEST),
    PLAN_LIMIT_REACHED("Gói hiện tại đã đăng ký tối đa khóa học", HttpStatus.BAD_REQUEST),
    ALREADY_ENROLLED("Bạn đã đăng ký khóa học này rồi.", HttpStatus.BAD_REQUEST),
    PAYMENT_REQUIRED("Thanh toán chưa thành công", HttpStatus.PAYMENT_REQUIRED),
    FREE_NOT_ALLOW_ASSIGN("Gói miễn phí không cho phép đăng ký khóa học", HttpStatus.PAYMENT_REQUIRED),
    SUBSCRIPTION_REQUIRED("Phải đăng ký gói mới được mở khóa khóa học.", HttpStatus.PAYMENT_REQUIRED),
    COURSE_NOT_COMPLETED("Bạn phải hoàn thành tất cả các bài học trước khi xem chứng chỉ.", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
