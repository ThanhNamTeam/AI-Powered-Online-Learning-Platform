package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.request.LoginRequest;
import com.minhkhoi.swd392.dto.request.RefreshTokenRequest;
import com.minhkhoi.swd392.dto.request.SendOtpRequest;
import com.minhkhoi.swd392.dto.request.ValidateTokenRequest;
import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.dto.response.LoginResponse;
import com.minhkhoi.swd392.dto.response.ValidateTokenResponse;
import com.minhkhoi.swd392.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for authentication and token management")
public class AuthenticationController {

    private final UserService userService;

    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP", description = "Send OTP code to email for registration")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        userService.sendOtpForRegistration(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(
                "OTP has been sent to your email. Please check your inbox.", null));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user and return access token and refresh token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh Token", description = "Refresh access token using refresh token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse loginResponse = userService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", loginResponse));
    }

    @PostMapping("/validate-token")
    @Operation(summary = "Validate Token", description = "Validate access token and return token validity status with user info")
    public ResponseEntity<ApiResponse<ValidateTokenResponse>> validateToken(@Valid @RequestBody ValidateTokenRequest request) {
        ValidateTokenResponse response = userService.validateToken(request.getToken());

        // Nếu token xàm, trả về status 401 Unauthorized
        if (!response.isValid()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<ValidateTokenResponse>builder()
                            .success(false)
                            .message(response.getMessage())
                            .data(response)
                            .build());
        }

        return ResponseEntity.ok(ApiResponse.success("Token is valid", response));
    }
}

