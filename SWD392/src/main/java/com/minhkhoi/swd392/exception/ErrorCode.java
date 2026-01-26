package com.minhkhoi.swd392.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // User errors (1000-1999)
    USER_NOT_FOUND("User not found with id: %s", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS("Email already exists: %s", HttpStatus.CONFLICT),
    EMAIL_NOT_VERIFIED("Email has not been verified", HttpStatus.BAD_REQUEST),

    // Authentication errors (2000-2999)
    INVALID_CREDENTIALS("Mật khẩu hoặc email không chính xác", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("Unauthorized access", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("Access denied", HttpStatus.FORBIDDEN),
    REFRESH_TOKEN_INVALID("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED),

    // OTP errors (3000-3999)
    OTP_EXPIRED("OTP has expired", HttpStatus.BAD_REQUEST),
    OTP_INVALID("Invalid OTP code", HttpStatus.BAD_REQUEST),
    OTP_SEND_FAILED("Failed to send OTP email", HttpStatus.INTERNAL_SERVER_ERROR),

    // Validation errors (5000-5999)
    VALIDATION_ERROR("Validation failed", HttpStatus.BAD_REQUEST),
    INVALID_INPUT("Invalid input data", HttpStatus.BAD_REQUEST),

    // Server errors (9000-9999)
    INTERNAL_SERVER_ERROR("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNCATEGORIZED_ERROR("Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),

    // Service errors (8000-8999)
    FILE_UPLOAD_FAILED("Failed to upload file to Cloudinary", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_FAILED("Failed to delete file from Cloudinary", HttpStatus.INTERNAL_SERVER_ERROR),
    TRANSCRIPTION_FAILED("Failed to transcribe video", HttpStatus.INTERNAL_SERVER_ERROR),
    TRANSCRIPT_PARSE_FAILED("Failed to parse transcript response", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_FILE("Invalid file format or size", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}

