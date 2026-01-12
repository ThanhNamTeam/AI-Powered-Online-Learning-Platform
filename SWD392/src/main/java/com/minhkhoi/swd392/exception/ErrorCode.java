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
    INVALID_CREDENTIALS("Invalid email or password", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("Unauthorized access", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("Access denied", HttpStatus.FORBIDDEN),

    // OTP errors (3000-3999)
    OTP_EXPIRED("OTP has expired", HttpStatus.BAD_REQUEST),
    OTP_INVALID("Invalid OTP code", HttpStatus.BAD_REQUEST),
    OTP_SEND_FAILED("Failed to send OTP email", HttpStatus.INTERNAL_SERVER_ERROR),

    // Validation errors (5000-5999)
    VALIDATION_ERROR("Validation failed", HttpStatus.BAD_REQUEST),
    INVALID_INPUT("Invalid input data", HttpStatus.BAD_REQUEST),

    // Server errors (9000-9999)
    INTERNAL_SERVER_ERROR("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNCATEGORIZED_ERROR("Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}

