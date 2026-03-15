package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.AuthTokenPair;
import com.minhkhoi.swd392.dto.request.LoginRequest;
import com.minhkhoi.swd392.dto.request.SendOtpRequest;
import com.minhkhoi.swd392.dto.request.ValidateTokenRequest;
import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.dto.response.LoginResponse;
import com.minhkhoi.swd392.dto.response.ValidateTokenResponse;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthTokenPair authTokenPair = userService.login(request);

        ResponseCookie responseCookie = ResponseCookie.from("refreshToken", authTokenPair.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(authTokenPair.getAccessToken())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authorization, HttpServletResponse response) {
        String accessToken = authorization.replace("Bearer ", "");
        userService.logout(accessToken);



        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .domain("localhost")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        AuthTokenPair authTokenPair = userService.refreshToken(refreshToken);

        ResponseCookie responseCookie = ResponseCookie.from("refreshToken", authTokenPair.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(authTokenPair.getAccessToken())
                .build();

        return ResponseEntity.ok(
                ApiResponse.success("Token refreshed successfully", loginResponse)
        );
    }


    @PostMapping("/validate-token")
    @Operation(summary = "Validate Token", description = "Validate access token and return token validity status with user info")
    public ResponseEntity<ApiResponse<ValidateTokenResponse>> validateToken(@Valid @RequestBody ValidateTokenRequest request) {
        ValidateTokenResponse response = userService.validateToken(request.getToken());

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

