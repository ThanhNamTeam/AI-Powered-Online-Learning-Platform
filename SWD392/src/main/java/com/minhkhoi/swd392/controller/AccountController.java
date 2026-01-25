package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.request.CreateUserRequest;
import com.minhkhoi.swd392.dto.request.UpdateUserRequest;
import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.dto.response.UserResponse;
import com.minhkhoi.swd392.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "APIs for managing user accounts")
public class AccountController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(
            summary = "Get Current User Info",
            description = "Get information of currently authenticated user (Requires JWT token - Use Authorize button)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        // Spring Security sẽ tự động validate token qua JwtAuthenticationFilter
        // Nếu đến đây nghĩa là token đã hợp lệ
        // Lấy email từ SecurityContext (đã được set trong JwtAuthenticationFilter)
        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User is not authenticated"));
        }

        String email = authentication.getName(); // username chính là email

        // Get user by email
        UserResponse userResponse = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("User info retrieved successfully", userResponse));
    }

    @PostMapping
    @Operation(summary = "Create a new user", description = "Create a new user account with OTP verification")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse userResponse = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", userResponse));
    }

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieve user details by user ID (Requires JWT token - Use Authorize button)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID userId) {
        UserResponse userResponse = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", userResponse));
    }

    @GetMapping("/email/{email}")
    @Operation(
            summary = "Get user by email",
            description = "Retrieve user details by email address (Requires JWT token - Use Authorize button)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        UserResponse userResponse = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", userResponse));
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve a list of all users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @PutMapping("/{userId}")
    @Operation(
            summary = "Update user",
            description = "Update user details by user ID (Requires JWT token - Use Authorize button)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse userResponse = userService.updateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", userResponse));
    }

    @DeleteMapping("/{userId}")
    @Operation(
            summary = "Delete user",
            description = "Delete user by user ID (Requires JWT token - Use Authorize button)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @GetMapping("/exists/{email}")
    @Operation(summary = "Check if email exists", description = "Check if a user with the given email already exists")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("Email check completed", exists));
    }
}
